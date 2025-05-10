const { validationResult, check } = require("express-validator");

/**
 * Middleware to handle validation errors
 */
const handleValidationErrors = (req, res, next) => {
  const errors = validationResult(req);
  if (!errors.isEmpty()) {
    return res.status(400).json({ errors: errors.array() });
  }
  next();
};

/**
 * Validation rules for user registration
 */
const registerValidation = [
  check("username")
    .trim()
    .notEmpty()
    .withMessage("Username is required")
    .isLength({ min: 3, max: 20 })
    .withMessage("Username must be between 3 and 20 characters"),
  check("email")
    .trim()
    .notEmpty()
    .withMessage("Email is required")
    .isEmail()
    .withMessage("Please provide a valid email"),
  check("password")
    .trim()
    .notEmpty()
    .withMessage("Password is required")
    .isLength({ min: 6 })
    .withMessage("Password must be at least 6 characters long"),
  handleValidationErrors,
];

/**
 * Validation rules for user login
 */
const loginValidation = [
  check("email")
    .trim()
    .notEmpty()
    .withMessage("Email is required")
    .isEmail()
    .withMessage("Please provide a valid email"),
  check("password").trim().notEmpty().withMessage("Password is required"),
  handleValidationErrors,
];

/**
 * Validation rules for saving progress
 */
const progressValidation = [
  check("points").isNumeric().withMessage("Points must be a number"),
  check("streak").isNumeric().withMessage("Streak must be a number"),
  check("lastDay").isNumeric().withMessage("Last day must be a number"),
  handleValidationErrors,
];

/**
 * Validation rules for saving task history
 */
const taskHistoryValidation = [
  check("quest").trim().notEmpty().withMessage("Quest is required"),
  check("points").isNumeric().withMessage("Points must be a number"),
  check("status")
    .isIn(["COMPLETED", "REJECTED"])
    .withMessage("Status must be either COMPLETED or REJECTED"),
  handleValidationErrors,
];

/**
 * Validation rules for theme preference
 */
const themeValidation = [
  check("themeMode")
    .isInt({ min: 0, max: 2 })
    .withMessage("Theme mode must be 0 (Light), 1 (Dark), or 2 (System)"),
  handleValidationErrors,
];

/**
 * Validation rules for creating/updating goals
 */
const goalValidation = [
  check("title")
    .trim()
    .notEmpty()
    .withMessage("Title is required")
    .isLength({ max: 100 })
    .withMessage("Title cannot exceed 100 characters"),
  check("description")
    .trim()
    .notEmpty()
    .withMessage("Description is required")
    .isLength({ max: 500 })
    .withMessage("Description cannot exceed 500 characters"),
  check("category")
    .isIn(["HEALTH", "CAREER", "EDUCATION", "PERSONAL", "FINANCE", "OTHER"])
    .withMessage("Invalid category"),
  check("difficulty")
    .isInt({ min: 1, max: 5 })
    .withMessage("Difficulty must be between 1 and 5"),
  check("deadline")
    .optional()
    .isISO8601()
    .withMessage("Deadline must be a valid date"),
  handleValidationErrors,
];

/**
 * Validation rules for updating goal progress
 */
const goalProgressValidation = [
  check("progressIncrement")
    .isNumeric()
    .withMessage("Progress increment must be a number")
    .custom((value) => value >= 0)
    .withMessage("Progress increment must be a positive number"),
  check("questId")
    .optional()
    .isMongoId()
    .withMessage("Invalid quest ID format"),
  handleValidationErrors,
];

module.exports = {
  registerValidation,
  loginValidation,
  progressValidation,
  taskHistoryValidation,
  themeValidation,
  goalValidation,
  goalProgressValidation,
};
