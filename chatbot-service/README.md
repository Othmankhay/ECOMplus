# 🤖 ECOMplus Chatbot Service

Microservice de Chatbot intelligent basé sur **RAG (Retrieval Augmented Generation)** avec intégration Telegram.

## 📋 Fonctionnalités

- **RAG (Retrieval Augmented Generation)**: Recherche sémantique dans les produits + génération de réponses contextuelles
- **Intégration Telegram**: Bot Telegram interactif pour les clients
- **API REST**: Endpoints pour intégration web/mobile
- **OpenAI GPT**: Utilisation de GPT-3.5-turbo pour des réponses naturelles
- **Vector Store**: Stockage vectoriel en mémoire pour recherche de similarité
- **Service Discovery**: Enregistrement automatique auprès d'Eureka

## 🚀 Configuration

### 1. Créer un Bot Telegram

1. Ouvrez Telegram et recherchez **@BotFather**
2. Envoyez `/newbot`
3. Suivez les instructions pour créer votre bot
4. Copiez le **token** fourni

### 2. Configurer les credentials

Modifiez le fichier `src/main/resources/application.properties`:

```properties
# Token du bot Telegram
telegram.bot.token=VOTRE_TOKEN_TELEGRAM
telegram.bot.username=VOTRE_NOM_DE_BOT

# Clé API OpenAI (déjà configurée)
spring.ai.openai.api-key=sk-proj-...
```

### 3. Démarrer le service

```bash
# Assurez-vous que Discovery Service (Eureka) est en cours d'exécution
cd chatbot-service
./mvnw spring-boot:run
```

## 📡 API Endpoints

| Endpoint | Méthode | Description |
|----------|---------|-------------|
| `/api/chatbot/health` | GET | Vérifier l'état du service |
| `/api/chatbot/chat` | POST | Envoyer un message au chatbot |
| `/api/chatbot/search` | GET | Rechercher des produits |
| `/api/chatbot/recommendations` | GET | Obtenir des recommandations |
| `/api/chatbot/refresh` | POST | Rafraîchir les données produits |

### Exemple d'utilisation

```bash
# Santé du service
curl http://localhost:8084/api/chatbot/health

# Envoyer un message
curl -X POST http://localhost:8084/api/chatbot/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "Quels sont vos smartphones disponibles?"}'

# Rechercher des produits
curl "http://localhost:8084/api/chatbot/search?query=laptop"
```

## 🤖 Commandes Telegram

| Commande | Description |
|----------|-------------|
| `/start` | Démarrer la conversation |
| `/help` | Afficher l'aide |
| `/products` | Voir les produits populaires |
| `/search [terme]` | Rechercher un produit |

## 🏗️ Architecture RAG

```
┌─────────────────┐     ┌──────────────────┐     ┌─────────────────┐
│   User Query    │────▶│  Vector Store    │────▶│  Similar Docs   │
│  (Telegram/API) │     │  (Embeddings)    │     │  (Products)     │
└─────────────────┘     └──────────────────┘     └────────┬────────┘
                                                          │
┌─────────────────┐     ┌──────────────────┐              │
│    Response     │◀────│   OpenAI LLM     │◀─────────────┘
│   (Natural)     │     │   (GPT-3.5)      │   Context + Query
└─────────────────┘     └──────────────────┘
```

### Flux de traitement

1. **Réception**: Message reçu via Telegram ou API REST
2. **Embedding**: Conversion du message en vecteur
3. **Recherche**: Recherche de similarité dans le Vector Store
4. **Contexte**: Récupération des produits pertinents
5. **Génération**: OpenAI génère une réponse contextuelle
6. **Réponse**: Envoi de la réponse à l'utilisateur

## 🔧 Technologies

- **Spring Boot 3.4.2**
- **Spring AI 1.0.0-M4** (OpenAI integration)
- **Spring Cloud 2024.0.0** (Eureka, OpenFeign)
- **Telegram Bots API 6.9.7.1**
- **Lombok**

## 📦 Structure du projet

```
chatbot-service/
├── src/main/java/com/ecommerce/chatbot/
│   ├── ChatbotServiceApplication.java
│   ├── config/
│   │   ├── TelegramBotProperties.java
│   │   └── RagProperties.java
│   ├── controller/
│   │   └── ChatbotController.java
│   ├── model/
│   │   ├── Product.java
│   │   ├── Customer.java
│   │   └── ChatMessage.java
│   ├── rag/
│   │   ├── RagService.java
│   │   └── SimpleVectorStore.java
│   ├── service/
│   │   ├── InventoryServiceClient.java
│   │   └── InventoryServiceClientFallback.java
│   └── telegram/
│       └── EcomChatBot.java
├── src/main/resources/
│   └── application.properties
└── pom.xml
```

## 🐛 Dépannage

### Le bot Telegram ne répond pas
- Vérifiez que le token est correct
- Assurez-vous qu'Eureka est en cours d'exécution
- Consultez les logs: `logging.level.com.ecommerce.chatbot=DEBUG`

### Pas de produits trouvés
- Vérifiez que l'Inventory Service est en cours d'exécution
- Appelez `/api/chatbot/refresh` pour rafraîchir les données

### Erreurs OpenAI
- Vérifiez que la clé API est valide
- Vérifiez les quotas de votre compte OpenAI

## 📝 License

Projet éducatif - ECOMplus Microservices
