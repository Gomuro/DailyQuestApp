const express = require("express");
const router = express.Router();
const {
  createGoal,
  getGoals,
  getGoalById,
  updateGoal,
  deleteGoal,
  updateGoalProgress,
  getActiveGoal,
} = require("../controllers/goalController");
const { protect } = require("../middlewares/authMiddleware");
const {
  goalValidation,
  goalProgressValidation,
} = require("../middlewares/validationMiddleware");

// Route to get the active goal (most recently updated active goal)
router.get("/active", protect, getActiveGoal);

// Routes for CRUD operations on goals
router
  .route("/")
  .post(goalValidation, protect, createGoal)
  .get(protect, getGoals);

router
  .route("/:id")
  .get(protect, getGoalById)
  .put(goalValidation, protect, updateGoal)
  .delete(protect, deleteGoal);

// Route for updating goal progress
router.patch(
  "/:id/progress",
  goalProgressValidation,
  protect,
  updateGoalProgress
);

module.exports = router;
