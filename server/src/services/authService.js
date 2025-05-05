const userRepository = require("../repositories/userRepository");
const progressRepository = require("../repositories/progressRepository");

/**
 * Auth Service - handles authentication logic
 */
class AuthService {
  /**
   * Register a new user
   * @param {Object} userData - User data (username, email, password)
   * @returns {Promise<Object>} User object and token
   * @throws {Error} If validation fails or user already exists
   */
  async register(userData) {
    const { username, email } = userData;

    // Check if email or username already exists
    if (await userRepository.emailExists(email)) {
      const error = new Error("Email already exists");
      error.statusCode = 400;
      throw error;
    }

    if (await userRepository.usernameExists(username)) {
      const error = new Error("Username already exists");
      error.statusCode = 400;
      throw error;
    }

    // Create the user
    const user = await userRepository.createUser(userData);

    // Create initial progress record for the user
    await progressRepository.createProgress(user._id);

    // Generate token
    const token = user.getSignedJwtToken();

    return {
      _id: user._id,
      username: user.username,
      email: user.email,
      totalPoints: user.totalPoints,
      currentStreak: user.currentStreak,
      token,
    };
  }

  /**
   * Login a user
   * @param {string} email - User email
   * @param {string} password - User password
   * @returns {Promise<Object>} User object and token
   * @throws {Error} If credentials are invalid
   */
  async login(email, password) {
    // Check if user exists
    const user = await userRepository.findUserByEmail(email);

    if (!user) {
      const error = new Error("Invalid credentials");
      error.statusCode = 401;
      throw error;
    }

    // Check if password matches
    const isMatch = await user.matchPassword(password);

    if (!isMatch) {
      const error = new Error("Invalid credentials");
      error.statusCode = 401;
      throw error;
    }

    // Generate token
    const token = user.getSignedJwtToken();

    return {
      _id: user._id,
      username: user.username,
      email: user.email,
      totalPoints: user.totalPoints,
      currentStreak: user.currentStreak,
      token,
    };
  }

  /**
   * Get current user data
   * @param {string} userId - User ID
   * @returns {Promise<Object>} User object
   * @throws {Error} If user not found
   */
  async getCurrentUser(userId) {
    const user = await userRepository.findUserById(userId);

    if (!user) {
      const error = new Error("User not found");
      error.statusCode = 404;
      throw error;
    }

    return {
      _id: user._id,
      username: user.username,
      email: user.email,
      totalPoints: user.totalPoints,
      currentStreak: user.currentStreak,
      lastClaimedDay: user.lastClaimedDay,
    };
  }
}

module.exports = new AuthService();
