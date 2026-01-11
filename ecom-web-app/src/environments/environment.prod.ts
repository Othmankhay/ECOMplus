export const environment = {
  production: true,
  
  // API Gateway URL - update for production
  apiUrl: 'https://api.yourdomain.com/api',
  
  // Keycloak Configuration - update for production
  keycloak: {
    url: 'https://auth.yourdomain.com',
    realm: 'ecommerce',
    clientId: 'ecom-frontend'
  }
};
