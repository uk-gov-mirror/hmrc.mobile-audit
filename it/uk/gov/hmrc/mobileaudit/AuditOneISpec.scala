package uk.gov.hmrc.mobileaudit
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.ServeEvent
import org.joda.time.DateTime
import org.scalatest.OptionValues
import play.api.libs.json._
import uk.gov.hmrc.mobileaudit.controllers.IncomingAuditEvent
import uk.gov.hmrc.mobileaudit.stubs.{AuditStub, AuthStub}
import uk.gov.hmrc.mobileaudit.utils.BaseISpec
import uk.gov.hmrc.play.audit.model.DataEvent

import scala.collection.JavaConverters._

/**
  * The unit tests check that various forms of `IncomingEvent` are correctly translated the the
  * equivalent `DataEvent`, so just do some basic sanity checks that the event that is sent matches
  * the event we generated
  */
class AuditOneISpec extends BaseISpec with OptionValues {
  implicit val jodaDateReads: Reads[DateTime]  = play.api.libs.json.JodaReads.DefaultJodaDateTimeReads
  implicit val readDataEvent: Reads[DataEvent] = Json.reads

  "when a single event sent to /audit-event" - {
    "it should be forwarded to the audit service" in {
      val auditSource = app.configuration.underlying.getString("auditSource")
      val testNino = "AA100000Z"
      val detail = Map("nino" -> testNino)

      val incomingEvent = IncomingAuditEvent(auditType, None, None, None, detail)

      AuthStub.userIsLoggedIn(testNino)
      AuditStub.respondToAuditWithNoBody
      AuditStub.respondToAuditMergedWithNoBody

      val response = await(wsUrl(auditEventUrl).post(Json.toJson(incomingEvent)))
      response.status shouldBe 204

      verifyAuditEventWasForwarded()

      val auditRequest: ServeEvent = getAllServeEvents.asScala.find(_.getRequest.getUrl == "/write/audit").value
      val dataEvent = Json.parse(auditRequest.getRequest.getBodyAsString).validate[DataEvent].get

      dataEvent.auditSource shouldBe auditSource
      dataEvent.auditType shouldBe incomingEvent.auditType
      dataEvent.detail.get("nino").value shouldBe testNino
    }
    "it should fail if the nino in the audit body does not match that of the bearer token" in {
      val authNino = "AA100000Z"
      val maliciousNIno = "OTHERNINO"
      val detail = Map("nino" -> maliciousNIno)

      val incomingEvent = IncomingAuditEvent(auditType, None, None, None, detail)

      AuthStub.userIsLoggedIn(authNino)
      AuditStub.respondToAuditWithNoBody
      AuditStub.respondToAuditMergedWithNoBody

      val response = await(wsUrl(auditEventUrl).post(Json.toJson(incomingEvent)))
      response.status shouldBe 401
      response.body shouldBe "Authorization failure [failed to validate Nino]"

      verifyAuditEventWasNotForwarded()
    }
    "it should fail if the detail section does not have a nino in the detail body" in {
      val nino = "AA100000X"
      val detail = Map("otherKey" -> nino)

      val incomingEvent = IncomingAuditEvent(auditType, None, None, None, detail)

      AuthStub.userIsLoggedIn(nino)
      AuditStub.respondToAuditWithNoBody
      AuditStub.respondToAuditMergedWithNoBody

      val response = await(wsUrl(auditEventUrl).post(Json.toJson(incomingEvent)))
      response.status shouldBe 400
      response.body shouldBe "Invalid details payload"
    }
    "it should fail if the journeyId is not supplied as a query parameter" in {
      val nino = "AA100000X"
      val detail = Map("otherKey" -> nino)

      val incomingEvent = (0 to 3).map { i =>
        IncomingAuditEvent(s"$auditType-$i", None, None, None, detail)
      }.toList

      AuthStub.userIsLoggedIn(nino)
      AuditStub.respondToAuditWithNoBody
      AuditStub.respondToAuditMergedWithNoBody

      val response = await(wsUrl("/audit-event").post(Json.toJson(incomingEvent)))
      response.status shouldBe 400
      response.body shouldBe "{\"statusCode\":400,\"message\":\"bad request\"}"
    }

    "it should return 400 without a journeyId" in {
      val testNino = "AA100000Z"
      val detail = Map("nino" -> testNino)

      val incomingEvent = IncomingAuditEvent(auditType, None, None, None, detail)

      AuthStub.userIsLoggedIn(testNino)
      AuditStub.respondToAuditWithNoBody
      AuditStub.respondToAuditMergedWithNoBody

      val response = await(wsUrl("/audit-event").post(Json.toJson(incomingEvent)))
      response.status shouldBe 400
    }

    "it should return 400 with an invalid journeyId" in {
      val testNino = "AA100000Z"
      val detail = Map("nino" -> testNino)

      val incomingEvent = IncomingAuditEvent(auditType, None, None, None, detail)

      AuthStub.userIsLoggedIn(testNino)
      AuditStub.respondToAuditWithNoBody
      AuditStub.respondToAuditMergedWithNoBody

      val response = await(wsUrl("/audit-event?journeyId=ThisIsAnInvalidJourneyId").post(Json.toJson(incomingEvent)))
      response.status shouldBe 400
    }
  }

  private def verifyAuditEventWasForwarded(): Unit =
    wireMockServer.verify(1,
                          postRequestedFor(urlPathEqualTo("/write/audit"))
                            .withHeader("content-type", equalTo("application/json")))

  private def verifyAuditEventWasNotForwarded(): Unit =
    wireMockServer.verify(0,
                          postRequestedFor(urlPathEqualTo("/write/audit"))
                            .withHeader("content-type", equalTo("application/json")))

}
