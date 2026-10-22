/* global SwaggerUIBundle */
SwaggerUIBundle({
    url: './openapi.json',
    dom_id: '#swagger-ui',
    deepLinking: true,
    withCredentials: true,
    validatorUrl: null,
    persistAuthorization: false,
    presets: [SwaggerUIBundle.presets.apis],
    layout: 'BaseLayout'
});