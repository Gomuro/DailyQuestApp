const authService = require("../../services/authService");
const userRepository = require("../../repositories/userRepository");
const progressRepository = require("../../repositories/progressRepository");

// Mock the repositories
jest.mock("../../repositories/userRepository");
jest.mock("../../repositories/progressRepository");

describe("Auth Service", () => {
  beforeEach(() => {
    // Clear all mock implementations
    jest.clearAllMocks();
  });

  describe("register", () => {
    it("should register a new user and return user with token", async () => {
      // Mock data
      const userData = {
        username: "testuser",
        email: "test@example.com",
        password: "password123",
      };

      const mockUser = {
        _id: "user123",
        username: "testuser",
        email: "test@example.com",
        totalPoints: 0,
        currentStreak: 0,
        getSignedJwtToken: jest.fn().mockReturnValue("token123"),
      };

      // Setup mocks
      userRepository.emailExists.mockResolvedValue(false);
      userRepository.usernameExists.mockResolvedValue(false);
      userRepository.createUser.mockResolvedValue(mockUser);
      progressRepository.createProgress.mockResolvedValue({});

      // Call the service
      const result = await authService.register(userData);

      // Assertions
      expect(userRepository.emailExists).toHaveBeenCalledWith(userData.email);
      expect(userRepository.usernameExists).toHaveBeenCalledWith(
        userData.username
      );
      expect(userRepository.createUser).toHaveBeenCalledWith(userData);
      expect(progressRepository.createProgress).toHaveBeenCalledWith(
        mockUser._id
      );
      expect(mockUser.getSignedJwtToken).toHaveBeenCalled();

      expect(result).toEqual({
        _id: mockUser._id,
        username: mockUser.username,
        email: mockUser.email,
        totalPoints: mockUser.totalPoints,
        currentStreak: mockUser.currentStreak,
        token: "token123",
      });
    });

    it("should throw error if email already exists", async () => {
      // Mock data
      const userData = {
        username: "testuser",
        email: "existing@example.com",
        password: "password123",
      };

      // Setup mocks
      userRepository.emailExists.mockResolvedValue(true);

      // Call the service and expect error
      await expect(authService.register(userData)).rejects.toThrow(
        "Email already exists"
      );

      // Assertions
      expect(userRepository.emailExists).toHaveBeenCalledWith(userData.email);
      expect(userRepository.createUser).not.toHaveBeenCalled();
    });

    it("should throw error if username already exists", async () => {
      // Mock data
      const userData = {
        username: "existinguser",
        email: "test@example.com",
        password: "password123",
      };

      // Setup mocks
      userRepository.emailExists.mockResolvedValue(false);
      userRepository.usernameExists.mockResolvedValue(true);

      // Call the service and expect error
      await expect(authService.register(userData)).rejects.toThrow(
        "Username already exists"
      );

      // Assertions
      expect(userRepository.emailExists).toHaveBeenCalledWith(userData.email);
      expect(userRepository.usernameExists).toHaveBeenCalledWith(
        userData.username
      );
      expect(userRepository.createUser).not.toHaveBeenCalled();
    });
  });

  describe("login", () => {
    it("should login a user and return user with token", async () => {
      // Mock data
      const email = "test@example.com";
      const password = "password123";

      const mockUser = {
        _id: "user123",
        username: "testuser",
        email: "test@example.com",
        totalPoints: 100,
        currentStreak: 5,
        matchPassword: jest.fn().mockResolvedValue(true),
        getSignedJwtToken: jest.fn().mockReturnValue("token123"),
      };

      // Setup mocks
      userRepository.findUserByEmail.mockResolvedValue(mockUser);

      // Call the service
      const result = await authService.login(email, password);

      // Assertions
      expect(userRepository.findUserByEmail).toHaveBeenCalledWith(email);
      expect(mockUser.matchPassword).toHaveBeenCalledWith(password);
      expect(mockUser.getSignedJwtToken).toHaveBeenCalled();

      expect(result).toEqual({
        _id: mockUser._id,
        username: mockUser.username,
        email: mockUser.email,
        totalPoints: mockUser.totalPoints,
        currentStreak: mockUser.currentStreak,
        token: "token123",
      });
    });

    it("should throw error if user not found", async () => {
      // Mock data
      const email = "nonexistent@example.com";
      const password = "password123";

      // Setup mocks
      userRepository.findUserByEmail.mockResolvedValue(null);

      // Call the service and expect error
      await expect(authService.login(email, password)).rejects.toThrow(
        "Invalid credentials"
      );

      // Assertions
      expect(userRepository.findUserByEmail).toHaveBeenCalledWith(email);
    });

    it("should throw error if password is incorrect", async () => {
      // Mock data
      const email = "test@example.com";
      const password = "wrongpassword";

      const mockUser = {
        _id: "user123",
        username: "testuser",
        email: "test@example.com",
        matchPassword: jest.fn().mockResolvedValue(false),
      };

      // Setup mocks
      userRepository.findUserByEmail.mockResolvedValue(mockUser);

      // Call the service and expect error
      await expect(authService.login(email, password)).rejects.toThrow(
        "Invalid credentials"
      );

      // Assertions
      expect(userRepository.findUserByEmail).toHaveBeenCalledWith(email);
      expect(mockUser.matchPassword).toHaveBeenCalledWith(password);
    });
  });

  describe("getCurrentUser", () => {
    it("should return current user data", async () => {
      // Mock data
      const userId = "user123";

      const mockUser = {
        _id: "user123",
        username: "testuser",
        email: "test@example.com",
        totalPoints: 100,
        currentStreak: 5,
        lastClaimedDay: 123,
      };

      // Setup mocks
      userRepository.findUserById.mockResolvedValue(mockUser);

      // Call the service
      const result = await authService.getCurrentUser(userId);

      // Assertions
      expect(userRepository.findUserById).toHaveBeenCalledWith(userId);

      expect(result).toEqual({
        _id: mockUser._id,
        username: mockUser.username,
        email: mockUser.email,
        totalPoints: mockUser.totalPoints,
        currentStreak: mockUser.currentStreak,
        lastClaimedDay: mockUser.lastClaimedDay,
      });
    });

    it("should throw error if user not found", async () => {
      // Mock data
      const userId = "nonexistent123";

      // Setup mocks
      userRepository.findUserById.mockResolvedValue(null);

      // Call the service and expect error
      await expect(authService.getCurrentUser(userId)).rejects.toThrow(
        "User not found"
      );

      // Assertions
      expect(userRepository.findUserById).toHaveBeenCalledWith(userId);
    });
  });
});
