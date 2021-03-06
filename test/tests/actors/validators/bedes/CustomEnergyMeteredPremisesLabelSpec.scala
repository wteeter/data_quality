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

package tests.actors.validators.bedes

import java.util.UUID

import actors.validators.Validator
import actors.validators.Validator.UpdateObjectValidatedDocument
import actors.validators.bedes.CustomEnergyMeteredPremisesLabel
import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, _}
import com.maalka.bedes.{BEDESTransformTable, BedesDefinition}
import org.junit.runner.RunWith
import org.specs2.mutable.SpecificationLike
import org.specs2.runner.JUnitRunner


@RunWith(classOf[JUnitRunner])
class CustomEnergyMeteredPremisesLabelSpec() extends TestKit(ActorSystem("MySpec")) with ImplicitSender
  with SpecificationLike {
  sequential

  def after = system.terminate()

  "An empty BEDESTransformResult " should {
    "return invalid" in {
      val bedesDefinition = BedesDefinition()

      val transformResult = BEDESTransformTable.defaultTable.findBedesComposite(
        "Metered Areas (Energy)", None, None, bedesDefinition, false)

      system.actorOf(CustomEnergyMeteredPremisesLabel.props("", "", "", None, None)) ! Validator.Value(UUID.randomUUID(), Option(Seq(transformResult)))
      val validationResult = expectMsgClass(classOf[UpdateObjectValidatedDocument])
      validationResult.valid mustEqual false
    }
  }
  "An string BEDESTransformResult " should {
    "return invalid if not whole building" in {
      val bedesDefinition = BedesDefinition()

      val transformResult = BEDESTransformTable.defaultTable.findBedesComposite(
        "Metered Areas (Energy)", Some("now whole building"), None, bedesDefinition, false)

      system.actorOf(CustomEnergyMeteredPremisesLabel.props("", "", "", None, None)) ! Validator.Value(UUID.randomUUID(), Option(Seq(transformResult)))
      val validationResult = expectMsgClass(classOf[UpdateObjectValidatedDocument])
      Console.println(validationResult)
      validationResult.valid mustEqual false
    }
    "return valid if whole building" in {
      val bedesDefinition = BedesDefinition()

      val transformResult = BEDESTransformTable.defaultTable.findBedesComposite(
        "Metered Areas (Energy)", Some("Whole Building"), None, bedesDefinition, false)

      system.actorOf(CustomEnergyMeteredPremisesLabel.props("", "", "", None, None)) ! Validator.Value(UUID.randomUUID(), Option(Seq(transformResult)))
      val validationResult = expectMsgClass(classOf[UpdateObjectValidatedDocument])
      Console.println(validationResult)
      validationResult.valid mustEqual true
    }
  }
}
