const progressRepository = require("../repositories/progressRepository");
const userRepository = require("../repositories/userRepository");

/**
 * Progress Service - handles operations related to user progress
 */
class ProgressService {
  /**
   * Save user progress data
   * @param {string} userId - User ID
   * @param {Object} progressData - Progress data
   * @returns {Promise<Object>} Updated user data
   */
  async saveProgress(userId, progressData) {
    const { points, streak, lastDay } = progressData;

    // Update user progress data
    const updatedUser = await userRepository.updateUserProgress(userId, {
      points,
      streak,
      lastDay,
    });

    if (!updatedUser) {
      const error = new Error("User not found");
      error.statusCode = 404;
      throw error;
    }

    return {
      totalPoints: updatedUser.totalPoints,
      currentStreak: updatedUser.currentStreak,
      lastClaimedDay: updatedUser.lastClaimedDay,
    };
  }

  /**
   * Save daily quest seed
   * @param {string} userId - User ID
   * @param {number} seed - Seed value
   * @param {number} day - Day of year
   * @returns {Promise<Object>} Updated seed data
   */
  async saveSeed(userId, seed, day) {
    const progress = await progressRepository.updateSeed(userId, seed, day);

    return {
      currentSeed: progress.currentSeed,
      seedDay: progress.seedDay,
    };
  }

  /**
   * Save task history
   * @param {string} userId - User ID
   * @param {Object} taskData - Task data (quest, points, status)
   * @returns {Promise<Object>} Success message
   */
  async saveTaskHistory(userId, taskData) {
    await progressRepository.saveTaskHistory(userId, taskData);
    return { message: "Task history saved successfully" };
  }

  /**
   * Get task history
   * @param {string} userId - User ID
   * @returns {Promise<Array>} Task history array
   */
  async getTaskHistory(userId) {
    const taskHistory = await progressRepository.getTaskHistory(userId);
    return taskHistory;
  }

  /**
   * Clear task history
   * @param {string} userId - User ID
   * @returns {Promise<Object>} Success message
   */
  async clearTaskHistory(userId) {
    await progressRepository.clearTaskHistory(userId);
    return { message: "Task history cleared successfully" };
  }

  /**
   * Update reject information
   * @param {string} userId - User ID
   * @param {number} count - Reject count
   * @param {number} day - Day of year
   * @returns {Promise<Object>} Updated reject info
   */
  async updateRejectInfo(userId, count, day) {
    const progress = await progressRepository.updateRejectInfo(
      userId,
      count,
      day
    );

    return {
      rejectCount: progress.rejectCount,
      lastRejectDay: progress.lastRejectDay,
    };
  }

  /**
   * Save theme preference
   * @param {string} userId - User ID
   * @param {number} themeMode - Theme mode (0=Light, 1=Dark, 2=System)
   * @returns {Promise<Object>} Updated theme preference
   */
  async saveThemePreference(userId, themeMode) {
    const progress = await progressRepository.saveThemePreference(
      userId,
      themeMode
    );

    return {
      themePreference: progress.themePreference,
    };
  }

  /**
   * Get theme preference
   * @param {string} userId - User ID
   * @returns {Promise<Object>} Theme preference
   */
  async getThemePreference(userId) {
    const themePreference = await progressRepository.getThemePreference(userId);

    return {
      themePreference,
    };
  }
}

module.exports = new ProgressService();
