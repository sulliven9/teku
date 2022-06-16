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

package tech.pegasys.teku.ethereum.executionclient.metrics;

import static tech.pegasys.teku.ethereum.executionclient.metrics.MetricRecordingExecutionBuilderClient.RequestOutcome.ERROR;
import static tech.pegasys.teku.ethereum.executionclient.metrics.MetricRecordingExecutionBuilderClient.RequestOutcome.SUCCESS;

import java.util.List;
import java.util.Map;
import org.apache.tuweni.bytes.Bytes32;
import org.hyperledger.besu.plugin.services.MetricsSystem;
import tech.pegasys.teku.bls.BLSPublicKey;
import tech.pegasys.teku.ethereum.executionclient.ExecutionBuilderClient;
import tech.pegasys.teku.ethereum.executionclient.schema.Response;
import tech.pegasys.teku.infrastructure.async.SafeFuture;
import tech.pegasys.teku.infrastructure.metrics.MetricsCountersByIntervals;
import tech.pegasys.teku.infrastructure.metrics.TekuMetricCategory;
import tech.pegasys.teku.infrastructure.ssz.SszList;
import tech.pegasys.teku.infrastructure.time.TimeProvider;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;
import tech.pegasys.teku.spec.datastructures.blocks.SignedBeaconBlock;
import tech.pegasys.teku.spec.datastructures.execution.ExecutionPayload;
import tech.pegasys.teku.spec.datastructures.execution.SignedBuilderBid;
import tech.pegasys.teku.spec.datastructures.execution.SignedValidatorRegistration;

public class MetricRecordingExecutionBuilderClient implements ExecutionBuilderClient {

  public static final String BUILDER_REQUESTS_COUNTER_NAME = "builder_requests_total";

  public static final String STATUS_METHOD = "status";
  public static final String REGISTER_VALIDATORS_METHOD = "register_validators";
  public static final String GET_HEADER_METHOD = "get_header";
  public static final String GET_PAYLOAD_METHOD = "get_payload";

  private final ExecutionBuilderClient delegate;
  private final TimeProvider timeProvider;

  private final MetricsCountersByIntervals builderRequestsCountersByIntervals;

  public MetricRecordingExecutionBuilderClient(
      final ExecutionBuilderClient delegate,
      final TimeProvider timeProvider,
      final MetricsSystem metricsSystem) {
    this.delegate = delegate;
    this.timeProvider = timeProvider;

    builderRequestsCountersByIntervals =
        MetricsCountersByIntervals.create(
            TekuMetricCategory.BEACON,
            metricsSystem,
            BUILDER_REQUESTS_COUNTER_NAME,
            "Counter recording the number of requests made to the builder by method, outcome and execution time interval",
            List.of("method", "outcome"),
            Map.of(List.of(), List.of(100L, 300L, 500L, 1000L, 2000L, 3000L, 5000L)));
  }

  @Override
  public SafeFuture<Response<Void>> status() {
    return countRequest(delegate::status, STATUS_METHOD);
  }

  @Override
  public SafeFuture<Response<Void>> registerValidators(
      final UInt64 slot, final SszList<SignedValidatorRegistration> signedValidatorRegistrations) {
    return countRequest(
        () -> delegate.registerValidators(slot, signedValidatorRegistrations),
        REGISTER_VALIDATORS_METHOD);
  }

  @Override
  public SafeFuture<Response<SignedBuilderBid>> getHeader(
      final UInt64 slot, final BLSPublicKey pubKey, final Bytes32 parentHash) {
    return countRequest(() -> delegate.getHeader(slot, pubKey, parentHash), GET_HEADER_METHOD);
  }

  @Override
  public SafeFuture<Response<ExecutionPayload>> getPayload(
      final SignedBeaconBlock signedBlindedBeaconBlock) {
    return countRequest(() -> delegate.getPayload(signedBlindedBeaconBlock), GET_PAYLOAD_METHOD);
  }

  private <T> SafeFuture<Response<T>> countRequest(
      final RequestRunner<T> requestRunner, final String method) {
    final UInt64 startTime = timeProvider.getTimeInMillis();
    return requestRunner
        .run()
        .catchAndRethrow(__ -> recordRequestError(startTime, method))
        .thenPeek(
            response -> {
              if (response.isFailure()) {
                recordRequestError(startTime, method);
              } else {
                recordRequestSuccess(startTime, method);
              }
            });
  }

  private void recordRequestSuccess(final UInt64 startTime, final String method) {
    recordRequest(startTime, method, SUCCESS);
  }

  private void recordRequestError(final UInt64 startTime, final String method) {
    recordRequest(startTime, method, ERROR);
  }

  private void recordRequest(
      final UInt64 startTime, final String method, final RequestOutcome requestOutcome) {
    final UInt64 duration = timeProvider.getTimeInMillis().minusMinZero(startTime);
    builderRequestsCountersByIntervals.recordValue(duration, method, requestOutcome.name());
  }

  @FunctionalInterface
  private interface RequestRunner<T> {
    SafeFuture<Response<T>> run();
  }

  enum RequestOutcome {
    SUCCESS,
    ERROR;
  }
}
