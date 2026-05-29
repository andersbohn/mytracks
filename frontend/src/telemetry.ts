import { OTLPTraceExporter } from '@opentelemetry/exporter-trace-otlp-http'
import { DocumentLoadInstrumentation } from '@opentelemetry/instrumentation-document-load'
import { FetchInstrumentation } from '@opentelemetry/instrumentation-fetch'
import { resourceFromAttributes } from '@opentelemetry/resources'
import { BatchSpanProcessor, WebTracerProvider } from '@opentelemetry/sdk-trace-web'

export function initTelemetry() {
  const exporter = new OTLPTraceExporter({ url: '/otlp/v1/traces' })

  const provider = new WebTracerProvider({
    resource: resourceFromAttributes({ 'service.name': 'mytracks-frontend' }),
    spanProcessors: [new BatchSpanProcessor(exporter)],
  })

  provider.register()

  new FetchInstrumentation().enable()
  new DocumentLoadInstrumentation().enable()
}
