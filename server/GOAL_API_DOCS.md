# Goal Management API Documentation

This document provides information about the Goal Management API endpoints in the DailyQuestApp.

## Base URL

All API endpoints are prefixed with `/api/goals`.

## Authentication

All endpoints require authentication using a JWT token in the Authorization header:

```
Authorization: Bearer <your_token>
```

## Endpoints

### Get Active Goal

Retrieves the user's currently active goal (most recently updated goal with "ACTIVE" status).

- **URL**: `/api/goals/active`
- **Method**: `GET`
- **Auth required**: Yes
- **Response**:
  - Success (200): Returns the active goal object
  - Not Found (404): No active goal found

### Create a Goal

Creates a new goal for the authenticated user.

- **URL**: `/api/goals`
- **Method**: `POST`
- **Auth required**: Yes
- **Request Body**:
  ```json
  {
    "title": "Learn React Native",
    "description": "Build 3 apps and learn all core concepts of React Native",
    "category": "EDUCATION",
    "difficulty": 4,
    "deadline": "2023-12-31T23:59:59Z" // Optional ISO date
  }
  ```
- **Response**:
  - Created (201): Returns the created goal object

### Get All Goals

Retrieves all goals for the authenticated user.

- **URL**: `/api/goals`
- **Method**: `GET`
- **Auth required**: Yes
- **Query Parameters**:
  - `status`: Filter by status (`ACTIVE`, `COMPLETED`, `ABANDONED`)
- **Response**:
  - Success (200): Returns an array of goal objects

### Get a Goal by ID

Retrieves a specific goal by its ID.

- **URL**: `/api/goals/:id`
- **Method**: `GET`
- **Auth required**: Yes
- **Response**:
  - Success (200): Returns the goal object
  - Not Found (404): Goal not found

### Update a Goal

Updates a specific goal by its ID.

- **URL**: `/api/goals/:id`
- **Method**: `PUT`
- **Auth required**: Yes
- **Request Body**: Same as the create endpoint, all fields are optional for updates
- **Response**:
  - Success (200): Returns the updated goal object
  - Not Found (404): Goal not found

### Delete a Goal

Deletes a specific goal by its ID.

- **URL**: `/api/goals/:id`
- **Method**: `DELETE`
- **Auth required**: Yes
- **Response**:
  - Success (200): `{ "success": true, "message": "Goal deleted" }`
  - Not Found (404): Goal not found

### Update Goal Progress

Updates the progress of a specific goal.

- **URL**: `/api/goals/:id/progress`
- **Method**: `PATCH`
- **Auth required**: Yes
- **Request Body**:
  ```json
  {
    "progressIncrement": 10, // Number 0-100 representing percentage points to add
    "questId": "questObjectId" // Optional, links a quest to this goal
  }
  ```
- **Response**:
  - Success (200): Returns the updated goal object
  - Not Found (404): Goal not found

## Data Models

### Goal Object

```json
{
  "_id": "60d21b4667d0d8992e610c85",
  "user": "60d21b4667d0d8992e610c85",
  "title": "Learn React Native",
  "description": "Build 3 apps and learn all core concepts of React Native",
  "category": "EDUCATION",
  "difficulty": 4,
  "deadline": "2023-12-31T23:59:59Z",
  "status": "ACTIVE",
  "progress": 35,
  "relatedQuestIds": ["60d21b4667d0d8992e610c85", "60d21b4667d0d8992e610c86"],
  "createdAt": "2023-01-01T12:00:00Z",
  "updatedAt": "2023-01-02T12:00:00Z",
  "completedAt": null
}
```

## Categories

The following categories are supported:

- `HEALTH`
- `CAREER`
- `EDUCATION`
- `PERSONAL`
- `FINANCE`
- `OTHER`

## Status Values

The following status values are supported:

- `ACTIVE` - Goal is currently being worked on
- `COMPLETED` - Goal has been achieved
- `ABANDONED` - Goal has been abandoned
