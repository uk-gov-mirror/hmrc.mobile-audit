/*
 * Copyright 2019 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.mobileaudit.schemas

import play.api.libs.json.{JsBoolean, JsObject, JsValue}

object Schema {

  /**
    * Transforms a JSON schema by recursively adding "additionalProperties": false to every object.
    *
    * The reason we do this in this test code instead of just adding
    * "additionalProperties": false to the schema file is because we want
    * this test to detect fields generated by the code that aren't in the
    * schema because that could indicate that we have given a field a
    * different name in the code from the schema BUT we don't want to commit
    * to consumers of the API that we will never add any fields.
    */
  def banAdditionalProperties(schema: JsValue): JsValue = {
    val augmented: JsValue = schema match {
      case o@JsObject(_) if (schema \ "type").asOpt[String].contains("object") =>
        o + ("additionalProperties" -> JsBoolean(false))
      case v => v
    }

    augmented match {
      case o@JsObject(_) =>
        JsObject(o.fields.map { case (k, v) => (k, banAdditionalProperties(v)) })
      case v => v
    }
  }

}
