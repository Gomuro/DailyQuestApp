# Daily Quest Server

This is the server component of the Daily Quest App, providing authentication and progress synchronization capabilities.

## Sprint "server-open-1"

### Scope & Deliverables

- User Authentication (register, login, logout)
- Progress Synchronization (save and retrieve user progress)
- Task History Management (save, retrieve, and clear task history)
- Theme Preference Storage (save and retrieve theme preferences)
- Unit and Integration Tests

### Architecture

This project follows a layered architecture pattern:

- **Controllers**: Handle HTTP requests and responses
- **Services**: Implement business logic
- **Repositories**: Handle data access and persistence
- **Models**: Define data structures and validation
- **Middlewares**: Provide cross-cutting concerns like authentication and validation
- **Utils**: Contain utility functions
- **Config**: Store configuration parameters

### Setup

1. Install dependencies:

   ```bash
   npm install
   ```

2. Create a `.env` file in the server root directory with the following content:

   ```
   NODE_ENV=development
   PORT=5000
   MONGO_URI=mongodb://localhost:27017/dailyquest
   JWT_SECRET=your_secret_key_here
   JWT_EXPIRE=30d
   ```

3. Run the server:

   ```bash
   # Development mode with auto-reload
   npm run dev

   # Production mode
   npm start
   ```

4. Run tests:

   ```bash
   # Run all tests
   npm test

   # Run unit tests only
   npm run test:unit

   # Run integration tests only
   npm run test:integration
   ```

### API Endpoints

#### Authentication

- `POST /api/auth/register` - Register a new user
- `POST /api/auth/login` - Login a user
- `GET /api/auth/me` - Get current user profile

#### Progress

- `POST /api/progress` - Save user progress (points, streak, last day)
- `POST /api/progress/seed` - Save current quest seed
- `POST /api/progress/task-history` - Save task history
- `GET /api/progress/task-history` - Get task history
- `DELETE /api/progress/task-history` - Clear task history
- `POST /api/progress/reject-info` - Update quest rejection information
- `POST /api/progress/theme` - Save theme preference
- `GET /api/progress/theme` - Get theme preference

### Security

- JWT-based authentication
- Password encryption with bcrypt
- Input validation with express-validator
- Protected routes with authentication middleware

### Future Sprints

Upcoming sprints will include:

- Sprint "server-open-2": Quest generation and management
- Sprint "server-open-3": Social features and achievements
- Sprint "server-open-4": Push notifications and reminders
