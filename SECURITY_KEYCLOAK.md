# 🔐 Sécurisation avec Keycloak

Ce document explique comment sécuriser l'architecture microservices ECOMplus avec Keycloak.

## 📋 Architecture de Sécurité

```
┌─────────────────────────────────────────────────────────────────────┐
│                         KEYCLOAK (Port 8080)                        │
│                      Realm: ecommerce                               │
│   ┌─────────────┐  ┌─────────────┐  ┌─────────────┐                │
│   │   ADMIN     │  │    USER     │  │   MANAGER   │                │
│   └─────────────┘  └─────────────┘  └─────────────┘                │
└─────────────────────────────────────────────────────────────────────┘
                              │ JWT Token
                              ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    ANGULAR FRONTEND (Port 4201)                     │
│                      keycloak-angular                               │
└─────────────────────────────────────────────────────────────────────┘
                              │ Bearer Token
                              ▼
┌─────────────────────────────────────────────────────────────────────┐
│                   API GATEWAY (Port 8888)                           │
│              Spring Security + OAuth2 Resource Server               │
│                      JWT Validation                                 │
└─────────────────────────────────────────────────────────────────────┘
                              │ Token Propagation
                              ▼
┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐
│  Customer   │  │  Inventory  │  │   Billing   │  │   Chatbot   │
│  Service    │  │   Service   │  │   Service   │  │   Service   │
│  (8081)     │  │   (8082)    │  │   (8083)    │  │   (8084)    │
└─────────────┘  └─────────────┘  └─────────────┘  └─────────────┘
```

## 🚀 Démarrage Rapide

### 1. Démarrer Keycloak avec Docker

```bash
docker-compose up -d keycloak
```

Attendez que Keycloak soit prêt (environ 30 secondes).

### 2. Accéder à la Console Admin

- **URL**: http://localhost:8080/admin
- **Username**: `admin`
- **Password**: `admin123`

### 3. Realm pré-configuré

Le realm `ecommerce` est automatiquement importé avec:

#### Utilisateurs de test:
| Username | Password | Rôles |
|----------|----------|-------|
| admin | admin123 | ADMIN, USER |
| user | user123 | USER |
| manager | manager123 | MANAGER, USER |

#### Clients configurés:
| Client ID | Type | Description |
|-----------|------|-------------|
| ecom-frontend | Public | Application Angular |
| ecom-gateway | Bearer-only | API Gateway |
| ecom-services | Confidential | Microservices backend |

## 📡 Configuration des Microservices

### Gateway Service (application.yml)

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:8080/realms/ecommerce
```

### Customer/Inventory/Billing Services

```properties
spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8080/realms/ecommerce
```

## 🔒 Règles d'Autorisation

### Gateway (SecurityConfig.java)

```java
.authorizeExchange(exchanges -> exchanges
    // Endpoints publics
    .pathMatchers("/actuator/**").permitAll()
    .pathMatchers("/api/public/**").permitAll()
    
    // Admin seulement
    .pathMatchers("/api/admin/**").hasRole("ADMIN")
    
    // Authentification requise
    .pathMatchers("/api/customers/**").authenticated()
    .pathMatchers("/api/products/**").authenticated()
    .anyExchange().authenticated()
)
```

## 🌐 Configuration Angular

### 1. Installation des dépendances

```bash
cd ecom-web-app
npm install keycloak-angular keycloak-js
```

### 2. Configuration (environment.ts)

```typescript
export const environment = {
  keycloak: {
    url: 'http://localhost:8080',
    realm: 'ecommerce',
    clientId: 'ecom-frontend'
  }
};
```

### 3. Utilisation du AuthService

```typescript
import { AuthService } from './auth/auth.service';

@Component({...})
export class MyComponent {
  auth = inject(AuthService);

  login() {
    this.auth.login();
  }

  logout() {
    this.auth.logout();
  }

  get isLoggedIn() {
    return this.auth.isLoggedIn();
  }

  get username() {
    return this.auth.getUsername();
  }
}
```

### 4. Protection des routes

```typescript
import { authGuard, adminGuard } from './auth/auth.guard';

export const routes: Routes = [
  { path: 'public', component: PublicComponent },
  { 
    path: 'dashboard', 
    component: DashboardComponent,
    canActivate: [authGuard]
  },
  { 
    path: 'admin', 
    component: AdminComponent,
    canActivate: [adminGuard]
  }
];
```

## 🔧 Commandes Utiles

### Docker

```bash
# Démarrer Keycloak
docker-compose up -d keycloak

# Voir les logs
docker-compose logs -f keycloak

# Arrêter Keycloak
docker-compose down
```

### Obtenir un Token (pour tests)

```bash
# Token pour l'utilisateur 'user'
curl -X POST "http://localhost:8080/realms/ecommerce/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=ecom-frontend" \
  -d "username=user" \
  -d "password=user123"
```

### Tester l'API avec Token

```bash
TOKEN="votre_access_token"
curl -H "Authorization: Bearer $TOKEN" http://localhost:8888/api/customers
```

## 📝 Personnalisation

### Ajouter un nouvel utilisateur

1. Accédez à http://localhost:8080/admin
2. Sélectionnez le realm `ecommerce`
3. Allez dans Users > Add user
4. Configurez les credentials et rôles

### Ajouter un nouveau rôle

1. Realm settings > Roles
2. Add role
3. Assignez aux utilisateurs dans Users > [user] > Role Mappings

## 🐛 Dépannage

### "Token is not active"
- Vérifiez que Keycloak est en cours d'exécution
- Le token a peut-être expiré

### "Invalid token issuer"
- Vérifiez l'issuer-uri dans la configuration
- Doit correspondre exactement à l'URL du realm

### CORS errors
- Les origines autorisées sont configurées dans le client Keycloak
- Vérifiez webOrigins dans la configuration du client

## 📚 Ressources

- [Keycloak Documentation](https://www.keycloak.org/documentation)
- [Spring Security OAuth2](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html)
- [keycloak-angular](https://github.com/mauriciovigolo/keycloak-angular)
