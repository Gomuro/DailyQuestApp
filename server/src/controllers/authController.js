const asyncHandler = require("express-async-handler");
const authService = require("../services/authService");

/**
 * @desc    Register a new user
 * @route   POST /api/auth/register
 * @access  Public
 */
const registerUser = asyncHandler(async (req, res) => {
  const userData = {
    username: req.body.username,
    email: req.body.email,
    password: req.body.password,
  };

  const user = await authService.register(userData);
  res.status(201).json(user);
});

/**
 * @desc    Login user
 * @route   POST /api/auth/login
 * @access  Public
 */
const loginUser = asyncHandler(async (req, res) => {
  const { email, password } = req.body;

  const user = await authService.login(email, password);
  res.status(200).json(user);
});

/**
 * @desc    Get current user
 * @route   GET /api/auth/me
 * @access  Private
 */
const getCurrentUser = asyncHandler(async (req, res) => {
  const userId = req.user._id;

  const user = await authService.getCurrentUser(userId);
  res.status(200).json(user);
});

module.exports = {
  registerUser,
  loginUser,
  getCurrentUser,
};
