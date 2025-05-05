const progressService = require("../../services/progressService");
const progressRepository = require("../../repositories/progressRepository");
const userRepository = require("../../repositories/userRepository");

// Mock the repositories
jest.mock("../../repositories/progressRepository");
jest.mock("../../repositories/userRepository");

describe("Progress Service", () => {
  beforeEach(() => {
    // Clear all mock implementations
    jest.clearAllMocks();
  });

  describe("saveProgress", () => {
    it("should save user progress and return updated data", async () => {
      // Mock data
      const userId = "user123";
      const progressData = {
        points: 100,
        streak: 5,
        lastDay: 123,
      };

      const mockUpdatedUser = {
        _id: userId,
        totalPoints: 100,
        currentStreak: 5,
        lastClaimedDay: 123,
      };

      // Setup mocks
      userRepository.updateUserProgress.mockResolvedValue(mockUpdatedUser);

      // Call the service
      const result = await progressService.saveProgress(userId, progressData);

      // Assertions
      expect(userRepository.updateUserProgress).toHaveBeenCalledWith(
        userId,
        progressData
      );

      expect(result).toEqual({
        totalPoints: mockUpdatedUser.totalPoints,
        currentStreak: mockUpdatedUser.currentStreak,
        lastClaimedDay: mockUpdatedUser.lastClaimedDay,
      });
    });

    it("should throw error if user not found", async () => {
      // Mock data
      const userId = "nonexistent123";
      const progressData = {
        points: 100,
        streak: 5,
        lastDay: 123,
      };

      // Setup mocks
      userRepository.updateUserProgress.mockResolvedValue(null);

      // Call the service and expect error
      await expect(
        progressService.saveProgress(userId, progressData)
      ).rejects.toThrow("User not found");

      // Assertions
      expect(userRepository.updateUserProgress).toHaveBeenCalledWith(
        userId,
        progressData
      );
    });
  });

  describe("saveSeed", () => {
    it("should save seed value and return updated data", async () => {
      // Mock data
      const userId = "user123";
      const seed = 987654321;
      const day = 123;

      const mockProgress = {
        currentSeed: seed,
        seedDay: day,
      };

      // Setup mocks
      progressRepository.updateSeed.mockResolvedValue(mockProgress);

      // Call the service
      const result = await progressService.saveSeed(userId, seed, day);

      // Assertions
      expect(progressRepository.updateSeed).toHaveBeenCalledWith(
        userId,
        seed,
        day
      );

      expect(result).toEqual({
        currentSeed: mockProgress.currentSeed,
        seedDay: mockProgress.seedDay,
      });
    });
  });

  describe("saveTaskHistory", () => {
    it("should save task history and return success message", async () => {
      // Mock data
      const userId = "user123";
      const taskData = {
        quest: "Test quest",
        points: 100,
        status: "COMPLETED",
      };

      // Setup mocks
      progressRepository.saveTaskHistory.mockResolvedValue({
        taskHistory: [taskData],
      });

      // Call the service
      const result = await progressService.saveTaskHistory(userId, taskData);

      // Assertions
      expect(progressRepository.saveTaskHistory).toHaveBeenCalledWith(
        userId,
        taskData
      );

      expect(result).toEqual({
        message: "Task history saved successfully",
      });
    });
  });

  describe("getTaskHistory", () => {
    it("should return task history array", async () => {
      // Mock data
      const userId = "user123";
      const mockTaskHistory = [
        {
          quest: "Test quest 1",
          points: 100,
          status: "COMPLETED",
          timestamp: new Date(),
        },
        {
          quest: "Test quest 2",
          points: 200,
          status: "REJECTED",
          timestamp: new Date(),
        },
      ];

      // Setup mocks
      progressRepository.getTaskHistory.mockResolvedValue(mockTaskHistory);

      // Call the service
      const result = await progressService.getTaskHistory(userId);

      // Assertions
      expect(progressRepository.getTaskHistory).toHaveBeenCalledWith(userId);
      expect(result).toEqual(mockTaskHistory);
    });
  });

  describe("clearTaskHistory", () => {
    it("should clear task history and return success message", async () => {
      // Mock data
      const userId = "user123";

      // Setup mocks
      progressRepository.clearTaskHistory.mockResolvedValue({
        taskHistory: [],
      });

      // Call the service
      const result = await progressService.clearTaskHistory(userId);

      // Assertions
      expect(progressRepository.clearTaskHistory).toHaveBeenCalledWith(userId);

      expect(result).toEqual({
        message: "Task history cleared successfully",
      });
    });
  });

  describe("updateRejectInfo", () => {
    it("should update reject info and return updated data", async () => {
      // Mock data
      const userId = "user123";
      const count = 3;
      const day = 123;

      const mockProgress = {
        rejectCount: count,
        lastRejectDay: day,
      };

      // Setup mocks
      progressRepository.updateRejectInfo.mockResolvedValue(mockProgress);

      // Call the service
      const result = await progressService.updateRejectInfo(userId, count, day);

      // Assertions
      expect(progressRepository.updateRejectInfo).toHaveBeenCalledWith(
        userId,
        count,
        day
      );

      expect(result).toEqual({
        rejectCount: mockProgress.rejectCount,
        lastRejectDay: mockProgress.lastRejectDay,
      });
    });
  });

  describe("saveThemePreference", () => {
    it("should save theme preference and return updated data", async () => {
      // Mock data
      const userId = "user123";
      const themeMode = 1; // Dark mode

      const mockProgress = {
        themePreference: themeMode,
      };

      // Setup mocks
      progressRepository.saveThemePreference.mockResolvedValue(mockProgress);

      // Call the service
      const result = await progressService.saveThemePreference(
        userId,
        themeMode
      );

      // Assertions
      expect(progressRepository.saveThemePreference).toHaveBeenCalledWith(
        userId,
        themeMode
      );

      expect(result).toEqual({
        themePreference: mockProgress.themePreference,
      });
    });
  });

  describe("getThemePreference", () => {
    it("should return theme preference", async () => {
      // Mock data
      const userId = "user123";
      const themePreference = 2; // System mode

      // Setup mocks
      progressRepository.getThemePreference.mockResolvedValue(themePreference);

      // Call the service
      const result = await progressService.getThemePreference(userId);

      // Assertions
      expect(progressRepository.getThemePreference).toHaveBeenCalledWith(
        userId
      );

      expect(result).toEqual({
        themePreference,
      });
    });
  });
});
