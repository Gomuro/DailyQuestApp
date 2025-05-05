const request = require("supertest");
const mongoose = require("mongoose");
const { MongoMemoryServer } = require("mongodb-memory-server");
const app = require("../../index");
const User = require("../../models/userModel");

let mongoServer;

// Setup and teardown
beforeAll(async () => {
  mongoServer = await MongoMemoryServer.create();
  const uri = mongoServer.getUri();

  const mongooseOpts = {
    useNewUrlParser: true,
    useUnifiedTopology: true,
  };

  await mongoose.connect(uri, mongooseOpts);
});

afterAll(async () => {
  await mongoose.disconnect();
  await mongoServer.stop();
});

beforeEach(async () => {
  // Clear database between tests
  await User.deleteMany({});
});

describe("Auth API", () => {
  describe("POST /api/auth/register", () => {
    it("should register a new user", async () => {
      const userData = {
        username: "testuser",
        email: "test@example.com",
        password: "password123",
      };

      const res = await request(app).post("/api/auth/register").send(userData);

      expect(res.statusCode).toEqual(201);
      expect(res.body).toHaveProperty("token");
      expect(res.body.username).toEqual(userData.username);
      expect(res.body.email).toEqual(userData.email);
      expect(res.body.totalPoints).toEqual(0);
      expect(res.body.currentStreak).toEqual(0);
    });

    it("should return error if required fields are missing", async () => {
      const userData = {
        username: "testuser",
        email: "test@example.com",
        // Missing password
      };

      const res = await request(app).post("/api/auth/register").send(userData);

      expect(res.statusCode).toEqual(400);
      expect(res.body).toHaveProperty("errors");
    });

    it("should return error if email already exists", async () => {
      // Create a user first
      const userData = {
        username: "testuser",
        email: "test@example.com",
        password: "password123",
      };

      await request(app).post("/api/auth/register").send(userData);

      // Try to create a user with same email
      const newUserData = {
        username: "testuser2",
        email: "test@example.com", // Same email
        password: "password123",
      };

      const res = await request(app)
        .post("/api/auth/register")
        .send(newUserData);

      expect(res.statusCode).toEqual(400);
      expect(res.body.message).toContain("Email already exists");
    });
  });

  describe("POST /api/auth/login", () => {
    it("should login a user with valid credentials", async () => {
      // Create a user first
      const userData = {
        username: "testuser",
        email: "test@example.com",
        password: "password123",
      };

      await request(app).post("/api/auth/register").send(userData);

      // Login
      const loginData = {
        email: "test@example.com",
        password: "password123",
      };

      const res = await request(app).post("/api/auth/login").send(loginData);

      expect(res.statusCode).toEqual(200);
      expect(res.body).toHaveProperty("token");
      expect(res.body.username).toEqual(userData.username);
      expect(res.body.email).toEqual(userData.email);
    });

    it("should return error for invalid credentials", async () => {
      // Create a user first
      const userData = {
        username: "testuser",
        email: "test@example.com",
        password: "password123",
      };

      await request(app).post("/api/auth/register").send(userData);

      // Login with wrong password
      const loginData = {
        email: "test@example.com",
        password: "wrongpassword",
      };

      const res = await request(app).post("/api/auth/login").send(loginData);

      expect(res.statusCode).toEqual(401);
      expect(res.body.message).toContain("Invalid credentials");
    });
  });

  describe("GET /api/auth/me", () => {
    it("should get current user profile", async () => {
      // Create a user and get token
      const userData = {
        username: "testuser",
        email: "test@example.com",
        password: "password123",
      };

      const registerRes = await request(app)
        .post("/api/auth/register")
        .send(userData);

      const token = registerRes.body.token;

      // Get current user profile
      const res = await request(app)
        .get("/api/auth/me")
        .set("Authorization", `Bearer ${token}`);

      expect(res.statusCode).toEqual(200);
      expect(res.body.username).toEqual(userData.username);
      expect(res.body.email).toEqual(userData.email);
    });

    it("should return error if not authenticated", async () => {
      const res = await request(app)
        .get("/api/auth/me")
        .set("Authorization", "Bearer invalidtoken");

      expect(res.statusCode).toEqual(401);
      expect(res.body.message).toContain("Not authorized");
    });
  });
});
