const Progress = require("../models/progressModel");

/**
 * Progress Repository - handles all database operations for user progress
 */
class ProgressRepository {
  /**
   * Create a new progress record for a user
   * @param {string} userId - User ID
   * @returns {Promise<Object>} Created progress object
   */
  async createProgress(userId) {
    return await Progress.create({
      user: userId,
    });
  }

  /**
   * Get progress data for a user
   * @param {string} userId - User ID
   * @returns {Promise<Object|null>} Progress object or null if not found
   */
  async getProgressByUserId(userId) {
    return await Progress.findOne({ user: userId });
  }

  /**
   * Update the seed value for a user's daily quest
   * @param {string} userId - User ID
   * @param {number} seed - Seed value
   * @param {number} day - Day of year
   * @returns {Promise<Object|null>} Updated progress object or null if not found
   */
  async updateSeed(userId, seed, day) {
    return await Progress.findOneAndUpdate(
      { user: userId },
      { currentSeed: seed, seedDay: day },
      { new: true, upsert: true }
    );
  }

  /**
   * Save a task history entry
   * @param {string} userId - User ID
   * @param {Object} taskData - Task data including quest, points, status
   * @returns {Promise<Object|null>} Updated progress object or null if not found
   */
  async saveTaskHistory(userId, taskData) {
    return await Progress.findOneAndUpdate(
      { user: userId },
      {
        $push: {
          taskHistory: {
            quest: taskData.quest,
            points: taskData.points,
            status: taskData.status,
            timestamp: new Date(),
          },
        },
      },
      { new: true, upsert: true }
    );
  }

  /**
   * Get task history for a user
   * @param {string} userId - User ID
   * @returns {Promise<Array>} Array of task history entries
   */
  async getTaskHistory(userId) {
    const progress = await Progress.findOne({ user: userId });
    return progress ? progress.taskHistory : [];
  }

  /**
   * Clear task history for a user
   * @param {string} userId - User ID
   * @returns {Promise<Object|null>} Updated progress object or null if not found
   */
  async clearTaskHistory(userId) {
    return await Progress.findOneAndUpdate(
      { user: userId },
      { taskHistory: [] },
      { new: true }
    );
  }

  /**
   * Update reject info for a user
   * @param {string} userId - User ID
   * @param {number} count - Reject count
   * @param {number} day - Day of year
   * @returns {Promise<Object|null>} Updated progress object or null if not found
   */
  async updateRejectInfo(userId, count, day) {
    return await Progress.findOneAndUpdate(
      { user: userId },
      { rejectCount: count, lastRejectDay: day },
      { new: true, upsert: true }
    );
  }

  /**
   * Save theme preference for a user
   * @param {string} userId - User ID
   * @param {number} themeMode - Theme mode (0=Light, 1=Dark, 2=System)
   * @returns {Promise<Object|null>} Updated progress object or null if not found
   */
  async saveThemePreference(userId, themeMode) {
    return await Progress.findOneAndUpdate(
      { user: userId },
      { themePreference: themeMode },
      { new: true, upsert: true }
    );
  }

  /**
   * Get theme preference for a user
   * @param {string} userId - User ID
   * @returns {Promise<number>} Theme preference (0=Light, 1=Dark, 2=System)
   */
  async getThemePreference(userId) {
    const progress = await Progress.findOne({ user: userId });
    return progress ? progress.themePreference : 2; // Default to system theme
  }
}

module.exports = new ProgressRepository();
