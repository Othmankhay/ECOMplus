export const environment = {
  production: false,

  // API Gateway URL
  apiUrl: 'http://localhost:8888/api',

  // Keycloak Configuration
  keycloak: {
    url: 'http://localhost:8080',
    realm: 'ecommerce',
    clientId: 'ecom-frontend',
  },
};
