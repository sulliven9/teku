/*
 * Copyright ConsenSys Software Inc., 2022
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

package tech.pegasys.teku.beacon.pow;

import tech.pegasys.teku.infrastructure.unsigned.UInt64;

public interface Eth1HeadTracker {

  void start();

  void stop();

  long subscribe(HeadUpdatedSubscriber subscriber);

  void unsubscribe(long subscriberId);

  interface HeadUpdatedSubscriber {

    void onHeadUpdated(final UInt64 canonicalHead);
  }
}