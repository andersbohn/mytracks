import { CompositePropagator, W3CBaggagePropagator, W3CTraceContextPropagator } from '@opentelemetry/core'
import { OTLPTraceExporter } from '@opentelemetry/exporter-trace-otlp-http'
import { DocumentLoadInstrumentation } from '@opentelemetry/instrumentation-document-load'
import { FetchInstrumentation } from '@opentelemetry/instrumentation-fetch'
import { registerInstrumentations } from '@opentelemetry/instrumentation'
import { resourceFromAttributes } from '@opentelemetry/resources'
import { BatchSpanProcessor, WebTracerProvider } from '@opentelemetry/sdk-trace-web'

export function initTelemetry() {
  const exporter = new OTLPTraceExporter({ url: '/mytracks/api/otlp/v1/traces' })

  const provider = new WebTracerProvider({
    resource: resourceFromAttributes({ 'service.name': 'mytracks-frontend' }),
    spanProcessors: [new BatchSpanProcessor(exporter)],
  })

  provider.register({
    propagator: new CompositePropagator({
      propagators: [new W3CTraceContextPropagator(), new W3CBaggagePropagator()],
    }),
  })

  registerInstrumentations({
    instrumentations: [new FetchInstrumentation(), new DocumentLoadInstrumentation()],
    tracerProvider: provider,
  })
}
