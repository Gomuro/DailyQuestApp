const User = require("../models/userModel");

/**
 * User Repository - handles all database operations for users
 */
class UserRepository {
  /**
   * Create a new user
   * @param {Object} userData - User data including username, email, password
   * @returns {Promise<Object>} Created user object
   */
  async createUser(userData) {
    return await User.create(userData);
  }

  /**
   * Find a user by email
   * @param {string} email - Email to search for
   * @returns {Promise<Object|null>} User object or null if not found
   */
  async findUserByEmail(email) {
    return await User.findOne({ email }).select("+password");
  }

  /**
   * Find a user by username
   * @param {string} username - Username to search for
   * @returns {Promise<Object|null>} User object or null if not found
   */
  async findUserByUsername(username) {
    return await User.findOne({ username });
  }

  /**
   * Find a user by id
   * @param {string} id - User ID to search for
   * @returns {Promise<Object|null>} User object or null if not found
   */
  async findUserById(id) {
    return await User.findById(id);
  }

  /**
   * Update user progress data
   * @param {string} userId - User ID
   * @param {Object} progressData - Progress data to update (points, streak, lastDay)
   * @returns {Promise<Object|null>} Updated user object or null if not found
   */
  async updateUserProgress(userId, progressData) {
    return await User.findByIdAndUpdate(
      userId,
      {
        totalPoints: progressData.points,
        currentStreak: progressData.streak,
        lastClaimedDay: progressData.lastDay,
      },
      { new: true }
    );
  }

  /**
   * Check if email exists
   * @param {string} email - Email to check
   * @returns {Promise<boolean>} True if email exists, false otherwise
   */
  async emailExists(email) {
    const user = await User.findOne({ email });
    return !!user;
  }

  /**
   * Check if username exists
   * @param {string} username - Username to check
   * @returns {Promise<boolean>} True if username exists, false otherwise
   */
  async usernameExists(username) {
    const user = await User.findOne({ username });
    return !!user;
  }
}

module.exports = new UserRepository();
