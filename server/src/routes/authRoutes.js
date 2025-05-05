const express = require("express");
const router = express.Router();
const {
  registerUser,
  loginUser,
  getCurrentUser,
} = require("../controllers/authController");
const { protect } = require("../middlewares/authMiddleware");
const {
  registerValidation,
  loginValidation,
} = require("../middlewares/validationMiddleware");

// Public routes
router.post("/register", registerValidation, registerUser);
router.post("/login", loginValidation, loginUser);

// Protected routes
router.get("/me", protect, getCurrentUser);

module.exports = router;
