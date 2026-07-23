const dialog = document.querySelector("[data-dialog]");
const toast = document.querySelector("[data-toast]");
let toastTimer;
let dialogTrigger;

function openDialog(trigger) {
  dialogTrigger = trigger;
  dialog.hidden = false;
  document.body.style.overflow = "hidden";
  dialog.querySelector("[data-close-dialog]")?.focus();
}

function closeDialog() {
  dialog.hidden = true;
  document.body.style.overflow = "";
  dialogTrigger?.focus();
}

function showToast(message) {
  window.clearTimeout(toastTimer);
  toast.querySelector("p").textContent = message;
  toast.hidden = false;
  toastTimer = window.setTimeout(() => {
    toast.hidden = true;
  }, 3200);
}

document.addEventListener("click", (event) => {
  const openDialogButton = event.target.closest("[data-open-dialog]");
  if (openDialogButton) openDialog(openDialogButton);

  if (event.target.closest("[data-close-dialog]")) closeDialog();

  if (event.target.closest("[data-confirm-dialog]")) {
    closeDialog();
    showToast("处决已记录，并已完成胜负条件检查。");
  }

  if (event.target.closest("[data-show-toast]")) {
    showToast("投毒者步骤已完成，正在进入下一步。");
  }

  if (event.target.closest("[data-close-toast]")) {
    window.clearTimeout(toastTimer);
    toast.hidden = true;
  }

  const choice = event.target.closest("[data-choice-group] .choice");
  if (choice) {
    const group = choice.closest("[data-choice-group]");
    group.querySelectorAll(".choice").forEach((item) => {
      item.setAttribute("aria-pressed", String(item === choice));
    });
  }

  if (event.target === dialog) closeDialog();
});

document.addEventListener("keydown", (event) => {
  if (event.key === "Escape" && !dialog.hidden) closeDialog();
});
