const express = require("express");
const router = express.Router();
const {
  saveProgress,
  saveSeed,
  saveTaskHistory,
  getTaskHistory,
  clearTaskHistory,
  updateRejectInfo,
  saveThemePreference,
  getThemePreference,
} = require("../controllers/progressController");
const { protect } = require("../middlewares/authMiddleware");
const {
  progressValidation,
  taskHistoryValidation,
  themeValidation,
} = require("../middlewares/validationMiddleware");

// All routes are protected
router.post("/", progressValidation, protect, saveProgress);
router.post("/seed", protect, saveSeed);
router.post("/task-history", taskHistoryValidation, protect, saveTaskHistory);
router.get("/task-history", protect, getTaskHistory);
router.delete("/task-history", protect, clearTaskHistory);
router.post("/reject-info", protect, updateRejectInfo);
router.post("/theme", themeValidation, protect, saveThemePreference);
router.get("/theme", protect, getThemePreference);

module.exports = router;
