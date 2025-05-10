# Daily Quest Server

This is the server component of the Daily Quest App, providing authentication and progress synchronization capabilities.

## Sprint "server-open-1"

### Scope & Deliverables

- User Authentication (register, login, logout)
- Progress Synchronization (save and retrieve user progress)
- Task History Management (save, retrieve, and clear task history)
- Theme Preference Storage (save and retrieve theme preferences)
- Unit and Integration Tests

## Sprint "server-open-2"

### Scope & Deliverables

- Goal Management (create, update, delete goals)
- Goal Progress Tracking (track progress towards goals)
- Goal-Based Quest Integration (connect quests to goals)
- Enhanced Task History (relate tasks to goals)

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

#### Goals

- `POST /api/goals` - Create a new goal
- `GET /api/goals` - Get all goals (with optional status filter)
- `GET /api/goals/active` - Get the current active goal
- `GET /api/goals/:id` - Get a specific goal
- `PUT /api/goals/:id` - Update a goal
- `DELETE /api/goals/:id` - Delete a goal
- `PATCH /api/goals/:id/progress` - Update goal progress

Detailed documentation for the Goal API is available in [GOAL_API_DOCS.md](./GOAL_API_DOCS.md).

### Security

- JWT-based authentication
- Password encryption with bcrypt
- Input validation with express-validator
- Protected routes with authentication middleware

### Future Sprints

Upcoming sprints will include:

- Sprint "server-open-3": Social features and achievements
- Sprint "server-open-4": Push notifications and reminders
