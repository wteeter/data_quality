/*
 * Copyright 2017 Maalka
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package actors.validators.bedes

import java.util.UUID

import actors.ValidatorActors.BedesValidators.{BedesValidator, BedesValidatorCompanion}
import actors.validators.Validator
import actors.validators.Validator.MapValid
import actors.validators.basic.{Exists, Numeric, WithinRange}
import akka.actor.{ActorSystem, Props}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import com.maalka.bedes.BEDESTransformResult
import play.api.libs.json.{JsObject, Json}

import scala.concurrent.Future

object GrossFloorArea  extends  BedesValidatorCompanion {

  def props(guid: String,
            name: String,
            propertyId: String,
            validatorCategory: Option[String],
            arguments: Option[JsObject] = None)(implicit actorSystem: ActorSystem): Props =
    Props(new GrossFloorArea(guid, name, propertyId, validatorCategory, arguments))
}

/**
  * @param guid - guid
  * @param name - validator name
  * @param propertyId - to document
  * @param validatorCategory - to document
  * @param arguments - should include argument 'expectedValue' that will be used for comparision
  */
case class GrossFloorArea(guid: String,
                                       name: String,
                                       propertyId: String,
                                       validatorCategory: Option[String],
                                       override val arguments: Option[JsObject] = None)(implicit actorSystem: ActorSystem) extends BedesValidator {

  // the materializer to use.  this must be an ActorMaterializer

  implicit val materializer = ActorMaterializer()

  val validator = "bedeas_gross_floor_area"
  val bedesCompositeName = play.Play.application.configuration.getString("maalka.bedesGFACompositeField")


  val min:Option[Double] = Some(0)
  val max:Option[Double] = Some(3000000)

  val componentValidators = Seq(
    propsWrapper(Exists.props, None),
    propsWrapper(Numeric.props),
    propsWrapper(WithinRange.props, Option(Json.obj("min" -> min, "max" -> max))))

  def isValid(refId: UUID, value: Option[Seq[BEDESTransformResult]]): Future[Validator.MapValid] = {
    log.debug("Validating Gross Floor Area: {}", value)
    sourceValidateFromComponents(value).map { results =>
      log.debug("Validated Gross Floor Area: {}", results)
      if (!results.head.valid) {
        MapValid(valid = false, Option("%s does not exist".format(bedesCompositeName)))
      } else if (!results(1).valid) {
        MapValid(valid = false, Option("%s Floor Area is not a number".format(bedesCompositeName)))
      } else if (!results(2).valid) {
        formatMapValidRangeResponse(bedesCompositeName, min, max)
      } else {
        MapValid(valid = true, None)
      }
    }.runWith(Sink.head)
  }
}