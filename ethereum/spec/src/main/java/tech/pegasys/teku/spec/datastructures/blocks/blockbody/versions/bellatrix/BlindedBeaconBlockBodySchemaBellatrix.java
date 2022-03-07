/*
 * Copyright 2021 ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package tech.pegasys.teku.spec.datastructures.blocks.blockbody.versions.bellatrix;

import java.util.Optional;
import tech.pegasys.teku.spec.datastructures.blocks.blockbody.versions.altair.BeaconBlockBodySchemaAltair;
import tech.pegasys.teku.spec.datastructures.execution.ExecutionPayloadHeaderSchema;

public interface BlindedBeaconBlockBodySchemaBellatrix<T extends BlindedBeaconBlockBodyBellatrix>
    extends BeaconBlockBodySchemaAltair<T> {

  ExecutionPayloadHeaderSchema getExecutionPayloadHeaderSchema();

  @Override
  default Optional<BlindedBeaconBlockBodySchemaBellatrix<?>> toBlindedVersionBellatrix() {
    return Optional.of(this);
  }
}
