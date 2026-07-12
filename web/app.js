const STORAGE_KEY = "camp-board-game-host-web-v1";

const L = {
  zh: {
    appTitle: "营地桌游主持助手 Web",
    appSubtitle: "离线优先的手机网页主持台。先把人坐好，再选择游戏；页面会像导航一样告诉主持人下一步做什么。",
    players: "玩家与座位",
    commonPlayers: "常用玩家",
    addCommon: "保存常用玩家",
    addTemp: "加入临时玩家",
    tempName: "临时玩家名字",
    game: "游戏",
    start: "开始本局",
    restart: "重新开局",
    noSeats: "请先加入玩家座位。",
    seatOrder: "座位顺序",
    storytellerOnly: "说书人/主持人专用，包含隐藏身份。",
    doneNext: "完成，下一步",
    showToPlayer: "全屏展示给玩家",
    closeDisplay: "我已展示，返回",
    realAction: "真实行动",
    yes: "是",
    no: "否",
    wake: "需要唤醒",
    action: "说书人操作",
    tell: "要告诉玩家",
    explain: "新手解释",
    reason: "原因",
    day: "白天",
    night: "夜晚",
    dawn: "天亮了",
    firstNight: "第 1 夜",
    enterDay: "进入白天",
    startNight: "开始夜晚流程",
    noDeath: "无",
  },
  en: {
    appTitle: "Camp Board Game Host Web",
    appSubtitle: "Offline-first mobile host console. Seat the players, choose a game, then follow the guided steps.",
    players: "Players and seats",
    commonPlayers: "Frequent players",
    addCommon: "Save frequent player",
    addTemp: "Add guest player",
    tempName: "Guest player name",
    game: "Game",
    start: "Start game",
    restart: "Restart",
    noSeats: "Add players first.",
    seatOrder: "Seat order",
    storytellerOnly: "Storyteller/host only. Contains hidden identities.",
    doneNext: "Done, next step",
    showToPlayer: "Show full screen to player",
    closeDisplay: "Shown, return",
    realAction: "Real action",
    yes: "Yes",
    no: "No",
    wake: "Wake",
    action: "Storyteller action",
    tell: "Tell the player",
    explain: "New player explanation",
    reason: "Reason",
    day: "Day",
    night: "Night",
    dawn: "Dawn",
    firstNight: "Night 1",
    enterDay: "Enter day",
    startNight: "Start night flow",
    noDeath: "None",
  },
};

const GAMES = [
  { id: "clocktower", zh: "血染钟楼", en: "Clocktower" },
  { id: "werewolf", zh: "狼人杀", en: "Werewolf" },
  { id: "undercover", zh: "谁是卧底", en: "Undercover" },
];

const TB_ROLES = [
  ["washerwoman", "townsfolk", "洗衣妇", "Washerwoman"],
  ["librarian", "townsfolk", "图书管理员", "Librarian"],
  ["investigator", "townsfolk", "调查员", "Investigator"],
  ["chef", "townsfolk", "厨师", "Chef"],
  ["empath", "townsfolk", "共情者", "Empath"],
  ["fortuneTeller", "townsfolk", "占卜师", "Fortune Teller"],
  ["undertaker", "townsfolk", "送葬者", "Undertaker"],
  ["monk", "townsfolk", "僧侣", "Monk"],
  ["ravenkeeper", "townsfolk", "守鸦人", "Ravenkeeper"],
  ["virgin", "townsfolk", "处女", "Virgin"],
  ["slayer", "townsfolk", "猎手", "Slayer"],
  ["soldier", "townsfolk", "士兵", "Soldier"],
  ["mayor", "townsfolk", "市长", "Mayor"],
  ["butler", "outsider", "管家", "Butler"],
  ["drunk", "outsider", "酒鬼", "Drunk"],
  ["recluse", "outsider", "隐士", "Recluse"],
  ["saint", "outsider", "圣徒", "Saint"],
  ["poisoner", "minion", "投毒者", "Poisoner"],
  ["spy", "minion", "间谍", "Spy"],
  ["baron", "minion", "男爵", "Baron"],
  ["scarletWoman", "minion", "猩红女郎", "Scarlet Woman"],
  ["imp", "demon", "小恶魔", "Imp"],
].map(([id, team, zh, en]) => ({ id, team, zh, en }));

const state = loadState();

function defaultState() {
  return {
    language: "zh",
    selectedGame: "clocktower",
    commonPlayers: [
      { id: uid(), name: "A" },
      { id: uid(), name: "B" },
      { id: uid(), name: "C" },
      { id: uid(), name: "D" },
      { id: uid(), name: "E" },
    ],
    seats: [],
    displayCard: null,
    clocktower: freshClocktower(),
    werewolf: freshWerewolf(),
    undercover: freshUndercover(),
  };
}

function freshClocktower() {
  return {
    started: false,
    players: [],
    bluffs: [],
    phase: "firstNight",
    round: 1,
    nightStarted: false,
    nightIndex: 0,
    redHerring: null,
    poisonTarget: null,
    fortuneFirst: null,
    fortuneSecond: null,
    butlerMaster: null,
    pendingNightDeath: null,
    lastNightDeath: null,
    ravenkeeperTarget: null,
    lastExecuted: null,
    dayMode: "overview",
    nominator: null,
    nominee: null,
    voteCount: 0,
    highestVoteName: null,
    highestVoteCount: 0,
    selectedExecution: null,
    slayerTarget: null,
    slayerUsed: false,
    outcome: null,
    log: [],
  };
}

function freshWerewolf() {
  return {
    started: false,
    roles: [],
    phase: "night",
    round: 1,
    stepIndex: 0,
    nightDeath: null,
    checkedPlayer: null,
    poisonTarget: null,
    savedPlayer: null,
    log: [],
  };
}

function freshUndercover() {
  return {
    started: false,
    civilianWord: "露营",
    undercoverWord: "野餐",
    undercoverCount: 1,
    blankCount: 0,
    assignments: [],
    revealed: {},
  };
}

function loadState() {
  try {
    const saved = JSON.parse(localStorage.getItem(STORAGE_KEY) || "null");
    return saved ? { ...defaultState(), ...saved } : defaultState();
  } catch {
    return defaultState();
  }
}

function saveState() {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(state));
}

function uid() {
  return Math.random().toString(36).slice(2, 10) + Date.now().toString(36);
}

function tr(key) {
  return (L[state.language] && L[state.language][key]) || L.zh[key] || key;
}

function nameOfRole(roleId) {
  const role = TB_ROLES.find((item) => item.id === roleId);
  if (!role) return roleId || "";
  return state.language === "en" ? role.en : role.zh;
}

function teamOfRole(roleId) {
  return (TB_ROLES.find((item) => item.id === roleId) || {}).team;
}

function gameName(id) {
  const game = GAMES.find((item) => item.id === id);
  return game ? (state.language === "en" ? game.en : game.zh) : id;
}

function htmlEscape(value) {
  return String(value ?? "")
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#39;");
}

function shuffle(list) {
  const copy = [...list];
  for (let i = copy.length - 1; i > 0; i -= 1) {
    const j = Math.floor(Math.random() * (i + 1));
    [copy[i], copy[j]] = [copy[j], copy[i]];
  }
  return copy;
}

function takeRandom(list, count) {
  return shuffle(list).slice(0, Math.max(0, count));
}

function render() {
  document.documentElement.lang = state.language === "en" ? "en" : "zh-CN";
  const app = document.querySelector("#app");
  app.innerHTML = `
    <main class="app-shell">
      <section class="hero">
        <div class="toolbar">
          <div class="nav-pills">
            <button class="pill ${state.language === "zh" ? "active" : ""}" data-action="set-language" data-lang="zh">中文</button>
            <button class="pill ${state.language === "en" ? "active" : ""}" data-action="set-language" data-lang="en">English</button>
          </div>
          <button class="ghost" data-action="export-state">${state.language === "en" ? "Export backup" : "导出备份"}</button>
        </div>
        <h1>${tr("appTitle")}</h1>
        <p>${tr("appSubtitle")}</p>
      </section>

      <section class="grid">
        <aside class="card stack">
          ${renderPlayerPanel()}
        </aside>
        <section class="card stack">
          ${renderGamePanel()}
        </section>
      </section>

      <p class="footer-note">
        ${state.language === "en"
          ? "Offline note: after the first web visit, the service worker caches the app shell. Data stays in this browser, so export backups before clearing storage."
          : "离线提示：第一次通过网页访问后，Service Worker 会缓存应用外壳。数据保存在当前浏览器里，清理浏览器数据前请先导出备份。"}
      </p>
    </main>
    ${renderDisplayCard()}
  `;
}

function renderPlayerPanel() {
  return `
    <div class="toolbar">
      <h2>${tr("players")}</h2>
      <button class="quiet" data-action="clear-seats">${state.language === "en" ? "Clear seats" : "清空座位"}</button>
    </div>
    <div class="two">
      <label class="field">
        <span>${tr("commonPlayers")}</span>
        <input id="commonName" placeholder="${state.language === "en" ? "Name" : "玩家名字"}" />
      </label>
      <label class="field">
        <span>${tr("tempName")}</span>
        <input id="tempName" placeholder="${state.language === "en" ? "Guest name" : "临时玩家名字"}" />
      </label>
    </div>
    <div class="two">
      <button class="primary" data-action="add-common">${tr("addCommon")}</button>
      <button class="primary" data-action="add-temp">${tr("addTemp")}</button>
    </div>
    <div class="chip-list">
      ${state.commonPlayers.map((player, index) => `
        <button class="chip" data-action="add-common-seat" data-index="${index}">${htmlEscape(player.name)}</button>
      `).join("")}
    </div>
    <h3>${tr("seatOrder")}</h3>
    <div class="seat-list">
      ${state.seats.length === 0 ? `<div class="empty">${tr("noSeats")}</div>` : state.seats.map((player, index) => `
        <div class="seat-row">
          <div class="seat-number">${index + 1}</div>
          <div>
            <strong>${htmlEscape(player.name)}</strong>
            <div class="muted">${player.type === "common" ? tr("commonPlayers") : tr("tempName")}</div>
          </div>
          <div class="seat-actions">
            <button class="mini" data-action="move-seat" data-id="${player.id}" data-dir="-1">↑</button>
            <button class="mini" data-action="move-seat" data-id="${player.id}" data-dir="1">↓</button>
            <button class="mini" data-action="remove-seat" data-id="${player.id}">×</button>
          </div>
        </div>
      `).join("")}
    </div>
  `;
}

function renderGamePanel() {
  return `
    <div class="toolbar">
      <h2>${tr("game")}</h2>
      <div class="game-pills">
        ${GAMES.map((game) => `
          <button class="pill ${state.selectedGame === game.id ? "active" : ""}" data-action="select-game" data-game="${game.id}">
            ${gameName(game.id)}
          </button>
        `).join("")}
      </div>
    </div>
    ${state.selectedGame === "clocktower" ? renderClocktower() : ""}
    ${state.selectedGame === "werewolf" ? renderWerewolf() : ""}
    ${state.selectedGame === "undercover" ? renderUndercover() : ""}
  `;
}

function renderClocktower() {
  const c = state.clocktower;
  if (!c.started) {
    return `
      <div class="stack">
        <div class="instruction">
          <strong>血染钟楼：暗流涌动</strong>
          ${state.language === "en"
            ? "This first web build supports Trouble Brewing setup, first-night guidance, player display cards, day nominations, voting, executions, Slayer ability, Demon kills, and basic win checks."
            : "第一版 Web 已支持暗流涌动开局、首夜导航、玩家展示卡、白天提名投票、处决、猎手能力、恶魔夜杀和基础胜负检查。"}
        </div>
        <button class="primary" data-action="start-clocktower" ${state.seats.length < 5 ? "disabled" : ""}>
          ${tr("start")} ${state.seats.length < 5 ? `(${state.language === "en" ? "needs 5+" : "至少 5 人"})` : ""}
        </button>
      </div>
    `;
  }

  return `
    <div class="stack">
      ${renderClocktowerSummary()}
      ${c.outcome ? renderClocktowerOutcome() : renderClocktowerPhase()}
      ${renderClocktowerGrimoire()}
      <button class="danger" data-action="reset-clocktower">${tr("restart")}</button>
    </div>
  `;
}

function renderClocktowerSummary() {
  const c = state.clocktower;
  const alive = ctAlive();
  const threshold = Math.floor(alive.length / 2) + 1;
  const phaseText = c.phase === "firstNight" ? tr("firstNight") : c.phase === "night" ? `${tr("night")} ${c.round}` : c.phase === "dawn" ? tr("dawn") : `${tr("day")} ${c.round}`;
  return `
    <div class="host-progress">
      <strong>${phaseText}</strong>
      <span>${state.language === "en" ? "Alive" : "存活玩家"}：${alive.length} · ${state.language === "en" ? "Votes to execute" : "处决所需票数"}：${threshold}</span>
      <span>${state.language === "en" ? "Highest vote" : "当前最高票"}：${c.highestVoteName ? `${ctSeatLabelById(c.highestVoteName)}，${c.highestVoteCount}` : tr("noDeath")}</span>
    </div>
  `;
}

function renderClocktowerPhase() {
  const c = state.clocktower;
  if (c.phase === "firstNight" || c.phase === "night") return renderClocktowerNight();
  if (c.phase === "dawn") return renderClocktowerDawn();
  return renderClocktowerDay();
}

function renderClocktowerNight() {
  const c = state.clocktower;
  if (!c.nightStarted) {
    return `
      <div class="step-card">
        <h2>${state.language === "en" ? "Night is about to begin" : "夜晚即将开始"}</h2>
        <div class="instruction">
          <strong>${state.language === "en" ? "Say to all players" : "请对所有玩家说"}</strong>
          ${state.language === "en" ? "Everyone close your eyes, look down, and stay quiet." : "所有人请闭眼，低头，保持安静。"}
        </div>
        <div class="instruction warn">
          ${state.language === "en"
            ? "If you need to wake a player, tap or quietly signal them. Avoid saying character names aloud."
            : "如果需要唤醒某位玩家，请轻拍或轻声示意。尽量不要大声念出角色名称，避免泄露信息。"}
        </div>
        <button class="primary" data-action="ct-start-night">${tr("startNight")}</button>
      </div>
    `;
  }

  const steps = ctNightSteps();
  const step = steps[Math.min(c.nightIndex, steps.length - 1)];
  return `
    <div class="host-progress">
      <strong>${c.phase === "firstNight" ? tr("firstNight") : `${tr("night")} ${c.round}`}</strong>
      <span>${state.language === "en" ? "Step" : "步骤"} ${c.nightIndex + 1} / ${steps.length}</span>
      <span>${state.language === "en" ? "Current" : "当前阶段"}：${htmlEscape(step.title)}</span>
    </div>
    ${renderNightStep(step)}
  `;
}

function renderNightStep(step) {
  const note = !step.real && step.reason ? step.reason : step.tip || step.explain || step.reason || "";
  return `
    <div class="step-card host-step">
      <div class="step-kicker">${htmlEscape(step.title)} · ${step.real ? tr("realAction") : (state.language === "en" ? "Placeholder" : "占位")}</div>
      <h2 class="host-command">${htmlEscape(step.headline || ctStepHeadline(step))}</h2>
      <div class="step-actions">
        ${renderClocktowerActionControl(step)}
        ${step.tell ? `<div class="big-answer">${nl(step.tell)}</div>` : ""}
        <div class="button-row">
          ${step.display && step.control !== "fortune" ? `<button class="ghost" data-action="show-display" data-card="${htmlEscape(encodeURIComponent(JSON.stringify(step.display)))}">${tr("showToPlayer")}</button>` : ""}
          <button class="primary" data-action="ct-next-night">${tr("doneNext")}</button>
        </div>
      </div>
      ${note ? `<p class="step-note ${step.real ? "" : "warn"}">${nl(note)}</p>` : ""}
    </div>
  `;
}

function renderClocktowerActionControl(step) {
  if (!step.control) return "";
  const alive = ctAlive();
  if (step.control === "redHerring") {
    if (!step.real) return "";
    return renderChipPicker("ct-select", "redHerring", ctPlayers().filter((p) => ctTeam(p) !== "demon"), state.clocktower.redHerring);
  }
  if (step.control === "poisonTarget") return renderChipPicker("ct-select", "poisonTarget", alive, state.clocktower.poisonTarget, step.real);
  if (step.control === "butlerMaster") return renderChipPicker("ct-select", "butlerMaster", alive, state.clocktower.butlerMaster, step.real);
  if (step.control === "nightDeath") return renderChipPicker("ct-select", "pendingNightDeath", alive, state.clocktower.pendingNightDeath, step.real);
  if (step.control === "ravenkeeperTarget") return renderChipPicker("ct-select", "ravenkeeperTarget", alive.filter((p) => p.id !== step.actorId), state.clocktower.ravenkeeperTarget, step.real);
  if (step.control === "fortune") {
    const ready = state.clocktower.fortuneFirst && state.clocktower.fortuneSecond;
    return `
      <div class="fortune-control">
        <div class="number-picker">
          ${renderNumberPicker("ct-select", "fortuneFirst", alive, state.clocktower.fortuneFirst, step.real)}
        </div>
        <div class="number-picker">
          ${renderNumberPicker("ct-select", "fortuneSecond", alive.filter((p) => p.id !== state.clocktower.fortuneFirst), state.clocktower.fortuneSecond, step.real)}
        </div>
        <button class="primary query-button" data-action="ct-show-fortune" ${ready && step.real ? "" : "disabled"}>
          ${state.language === "en" ? "Check and show" : "查询并展示"}
        </button>
      </div>
    `;
  }
  return "";
}

function ctStepHeadline(step) {
  if (!step.real) {
    return state.language === "en" ? `${step.title} placeholder` : `${step.title} 的占位操作`;
  }
  if (step.actorId) {
    const actor = ctPlayers().find((p) => p.id === step.actorId);
    if (actor) return state.language === "en" ? `Wake ${ctSeatLabel(actor)}` : `唤醒 ${ctSeatLabel(actor)}`;
  }
  return step.wakeText || step.actionText || step.title;
}

function renderChipPicker(action, field, players, selected, enabled = true) {
  return `
    <div class="chip-list">
      ${players.map((p) => `
        <button class="chip ${selected === p.id ? "selected" : ""}" data-action="${action}" data-field="${field}" data-id="${p.id}" ${enabled ? "" : "disabled"}>
          ${ctSeatLabel(p)}
        </button>
      `).join("")}
    </div>
  `;
}

function renderNumberPicker(action, field, players, selected, enabled = true) {
  return `
    <div class="number-grid">
      ${players.map((p) => `
        <button class="number-chip ${selected === p.id ? "selected" : ""}" data-action="${action}" data-field="${field}" data-id="${p.id}" ${enabled ? "" : "disabled"} aria-label="${htmlEscape(ctSeatLabel(p))}">
          ${p.seat}
        </button>
      `).join("")}
    </div>
  `;
}

function renderClocktowerDawn() {
  const c = state.clocktower;
  const death = c.lastNightDeath ? ctSeatLabelById(c.lastNightDeath) : tr("noDeath");
  return `
    <div class="step-card">
      <h2>${tr("dawn")}</h2>
      <div class="instruction">
        <strong>${state.language === "en" ? "Say to all players" : "请对所有玩家说"}</strong>
        ${state.language === "en" ? "Dawn breaks. Everyone open your eyes." : "天亮了，所有人睁眼。"}
      </div>
      <div class="instruction">
        <strong>${state.language === "en" ? "Last night death" : "昨晚死亡"}</strong>
        ${death}
      </div>
      <div class="instruction">
        <strong>${state.language === "en" ? "Announce" : "请宣布"}</strong>
        ${c.lastNightDeath
          ? (state.language === "en" ? `${death} died last night.` : `昨晚，${death} 死亡。`)
          : (state.language === "en" ? "No one died last night." : "昨晚没有人死亡。")}
      </div>
      <button class="primary" data-action="ct-enter-day">${tr("enterDay")}</button>
    </div>
  `;
}

function renderClocktowerDay() {
  const c = state.clocktower;
  if (c.dayMode === "nomination") return renderNomination();
  if (c.dayMode === "vote") return renderVote();
  if (c.dayMode === "endConfirm") return renderEndDay();
  return renderDayOverview();
}

function renderDayOverview() {
  const c = state.clocktower;
  const slayer = ctAlive().find((p) => p.actualRole === "slayer");
  return `
    <div class="step-card">
      <h2>${state.language === "en" ? `Day ${c.round}` : `第 ${c.round} 天 白天`}</h2>
      <div class="instruction">
        ${state.language === "en"
          ? "Manage nominations, votes, execution, dead votes, one-use abilities, and win checks."
          : "管理提名、投票、处决、亡者票、一次性能力和胜负检查。"}
      </div>
      ${slayer && !c.slayerUsed ? renderSlayerAbility(slayer) : ""}
      <button class="primary" data-action="ct-day-mode" data-mode="nomination">${state.language === "en" ? "Start nomination" : "开始提名"}</button>
      <button class="ghost" data-action="ct-day-mode" data-mode="endConfirm">${state.language === "en" ? "End day" : "结束白天"}</button>
    </div>
  `;
}

function renderSlayerAbility(slayer) {
  return `
    <div class="instruction danger-note">
      <strong>${state.language === "en" ? "Available ability" : "可发动能力"}</strong>
      ${ctSeatLabel(slayer)} ${state.language === "en" ? "is the Slayer. Once per game, choose a player. If they are the Demon, they die." : "是猎手。猎手可以在白天选择一名玩家；如果目标是恶魔，目标死亡。"}
      ${renderChipPicker("ct-select", "slayerTarget", ctAlive().filter((p) => p.id !== slayer.id), state.clocktower.slayerTarget)}
      <button class="danger" data-action="ct-fire-slayer" ${state.clocktower.slayerTarget ? "" : "disabled"}>${state.language === "en" ? "Use Slayer ability" : "发动猎手能力"}</button>
    </div>
  `;
}

function renderNomination() {
  const c = state.clocktower;
  return `
    <div class="step-card">
      <h2>${state.language === "en" ? "Nomination" : "提名"}</h2>
      <div class="instruction">
        <strong>${state.language === "en" ? "Choose nominator" : "选择提名人"}</strong>
        ${renderChipPicker("ct-select", "nominator", ctAlive(), c.nominator)}
      </div>
      <div class="instruction">
        <strong>${state.language === "en" ? "Choose nominee" : "选择被提名人"}</strong>
        ${renderChipPicker("ct-select", "nominee", ctAlive(), c.nominee)}
      </div>
      ${c.nominator && c.nominee ? `<div class="instruction"><strong>${state.language === "en" ? "Announce" : "请宣布"}</strong>${ctSeatLabelById(c.nominator)} ${state.language === "en" ? "nominates" : "提名"} ${ctSeatLabelById(c.nominee)}。</div>` : ""}
      <button class="primary" data-action="ct-day-mode" data-mode="vote" ${c.nominator && c.nominee ? "" : "disabled"}>${state.language === "en" ? "Start vote" : "开始投票"}</button>
    </div>
  `;
}

function renderVote() {
  const c = state.clocktower;
  const threshold = Math.floor(ctAlive().length / 2) + 1;
  const reached = c.voteCount >= threshold;
  return `
    <div class="step-card">
      <h2>${state.language === "en" ? "Vote" : "投票"}</h2>
      <div class="instruction">
        <strong>${state.language === "en" ? "Voting on" : "正在投票"}</strong>
        ${state.language === "en" ? "Execute" : "是否处决"} ${ctSeatLabelById(c.nominee)}
      </div>
      <div class="two">
        <button class="ghost" data-action="ct-vote-minus">-</button>
        <div class="big-answer">${c.voteCount}</div>
        <button class="ghost" data-action="ct-vote-plus">+</button>
      </div>
      <div class="instruction ${reached ? "" : "warn"}">
        ${ctSeatLabelById(c.nominee)} ${state.language === "en" ? `received ${c.voteCount} votes. ${reached ? "This reaches the execution threshold." : "This does not reach the threshold."}` : `获得 ${c.voteCount} 票。${reached ? "达到处决门槛。" : "未达到处决门槛。"}`}
      </div>
      <button class="primary" data-action="ct-save-vote">${state.language === "en" ? "Continue nominations" : "继续提名"}</button>
      <button class="ghost" data-action="ct-end-after-vote">${state.language === "en" ? "End day" : "结束白天"}</button>
    </div>
  `;
}

function renderEndDay() {
  const c = state.clocktower;
  const threshold = Math.floor(ctAlive().length / 2) + 1;
  const target = c.highestVoteCount >= threshold ? c.highestVoteName : null;
  return `
    <div class="step-card">
      <h2>${state.language === "en" ? "Prepare to end day" : "准备结束白天"}</h2>
      <div class="instruction">
        ${target
          ? (state.language === "en" ? `Current execution: ${ctSeatLabelById(target)} with ${c.highestVoteCount} votes.` : `当前将被处决：${ctSeatLabelById(target)}，票数：${c.highestVoteCount}。`)
          : (state.language === "en" ? "No player will be executed today." : "今天没有玩家被处决。")}
      </div>
      <button class="primary" data-action="ct-confirm-day">${target ? (state.language === "en" ? "Confirm execution" : "确认处决") : (state.language === "en" ? "Enter night" : "进入夜晚")}</button>
      <button class="ghost" data-action="ct-day-mode" data-mode="overview">${state.language === "en" ? "Return to day" : "返回白天"}</button>
    </div>
  `;
}

function renderClocktowerOutcome() {
  const outcome = state.clocktower.outcome;
  return `
    <div class="step-card">
      <h2>${state.language === "en" ? "Game over" : "游戏结束"}</h2>
      <div class="big-answer">${htmlEscape(outcome.winner)}</div>
      <div class="instruction">${htmlEscape(outcome.reason)}</div>
    </div>
  `;
}

function renderClocktowerGrimoire() {
  const c = state.clocktower;
  return `
    <div class="card stack">
      <h3>${state.language === "en" ? "Storyteller overview" : "说书人总览"}</h3>
      <p class="muted">${tr("storytellerOnly")}</p>
      <div class="seat-list">
        ${c.players.map((p) => {
          const role = `${nameOfRole(p.actualRole)}${p.shownRole !== p.actualRole ? ` / ${state.language === "en" ? "shown as" : "显示为"} ${nameOfRole(p.shownRole)}` : ""}`;
          const evil = ["minion", "demon"].includes(ctTeam(p));
          return `
            <div class="status-row">
              <div class="seat-number">${p.seat}</div>
              <div>
                <strong>${htmlEscape(p.name)}</strong>
                <div>${role}</div>
                <div class="muted">${p.alive ? (state.language === "en" ? "Alive" : "存活") : (state.language === "en" ? "Dead" : "死亡")}</div>
              </div>
              <span class="role-tag ${evil ? "evil" : ""}">${teamLabel(ctTeam(p))}</span>
            </div>
          `;
        }).join("")}
      </div>
    </div>
  `;
}

function renderWerewolf() {
  const w = state.werewolf;
  if (!w.started) {
    return `
      <div class="stack">
        <div class="instruction">
          <strong>${state.language === "en" ? "Werewolf basic judge" : "狼人杀基础法官助手"}</strong>
          ${state.language === "en" ? "Includes Werewolves, Seer, Witch, Hunter, villagers, night guidance, and basic day transition." : "包含狼人、预言家、女巫、猎人、村民的基础夜晚流程和白天推进。"}
        </div>
        <button class="primary" data-action="start-werewolf" ${state.seats.length < 5 ? "disabled" : ""}>${tr("start")}</button>
      </div>
    `;
  }
  const steps = werewolfSteps();
  const step = steps[Math.min(w.stepIndex, steps.length - 1)];
  return `
    <div class="host-progress"><strong>${state.language === "en" ? "Werewolf" : "狼人杀"} · ${w.phase === "night" ? tr("night") : tr("day")} ${w.round}</strong><span>${state.language === "en" ? "Step" : "步骤"} ${w.stepIndex + 1} / ${steps.length}</span></div>
    <div class="step-card">
      <h2>${htmlEscape(step.title)}</h2>
      <div class="instruction"><strong>${tr("wake")}</strong>${htmlEscape(step.wake)}</div>
      <div class="instruction"><strong>${tr("action")}</strong>${nl(step.action)}</div>
      ${renderWerewolfControl(step)}
      <button class="primary" data-action="werewolf-next">${tr("doneNext")}</button>
    </div>
    <div class="card stack">
      <h3>${state.language === "en" ? "Host role list" : "法官身份表"}</h3>
      ${w.roles.map((r, i) => `<div class="status-row"><div class="seat-number">${i + 1}</div><div><strong>${htmlEscape(r.name)}</strong><div>${htmlEscape(r.role)}</div></div><span class="role-tag ${r.team === "evil" ? "evil" : ""}">${r.team === "evil" ? (state.language === "en" ? "Evil" : "狼人") : (state.language === "en" ? "Good" : "好人")}</span></div>`).join("")}
    </div>
  `;
}

function renderWerewolfControl(step) {
  if (!step.field) return "";
  const players = state.werewolf.roles.map((r) => ({ id: r.id, seat: r.seat, name: r.name }));
  return renderGenericPicker("werewolf-select", step.field, players, state.werewolf[step.field]);
}

function renderGenericPicker(action, field, players, selected) {
  return `<div class="chip-list">${players.map((p) => `<button class="chip ${selected === p.id ? "selected" : ""}" data-action="${action}" data-field="${field}" data-id="${p.id}">${p.seat}号 ${htmlEscape(p.name)}</button>`).join("")}</div>`;
}

function renderUndercover() {
  const u = state.undercover;
  if (!u.started) {
    return `
      <div class="stack">
        <div class="two">
          <label class="field"><span>${state.language === "en" ? "Civilian word" : "平民词"}</span><input id="civilianWord" value="${htmlEscape(u.civilianWord)}" /></label>
          <label class="field"><span>${state.language === "en" ? "Undercover word" : "卧底词"}</span><input id="undercoverWord" value="${htmlEscape(u.undercoverWord)}" /></label>
        </div>
        <div class="two">
          <label class="field"><span>${state.language === "en" ? "Undercover count" : "卧底人数"}</span><input id="undercoverCount" type="number" min="1" value="${u.undercoverCount}" /></label>
          <label class="field"><span>${state.language === "en" ? "Blank count" : "白板人数"}</span><input id="blankCount" type="number" min="0" value="${u.blankCount}" /></label>
        </div>
        <button class="primary" data-action="start-undercover" ${state.seats.length < 3 ? "disabled" : ""}>${tr("start")}</button>
      </div>
    `;
  }
  return `
    <div class="stack">
      <div class="instruction"><strong>${state.language === "en" ? "Pass the phone around" : "依次传手机查看词语"}</strong>${state.language === "en" ? "Each player taps their own card, reads silently, then closes it." : "每位玩家点击自己的卡片，默读后关闭。主持人不要公开身份。"}</div>
      <div class="seat-list">
        ${u.assignments.map((a) => `
          <div class="seat-row">
            <div class="seat-number">${a.seat}</div>
            <div><strong>${htmlEscape(a.name)}</strong><div class="muted">${state.language === "en" ? "Tap to reveal privately" : "点击私下查看"}</div></div>
            <button class="mini" data-action="undercover-reveal" data-id="${a.id}">${state.language === "en" ? "Reveal" : "查看"}</button>
          </div>
        `).join("")}
      </div>
      <button class="danger" data-action="reset-undercover">${tr("restart")}</button>
    </div>
  `;
}

function renderDisplayCard() {
  const card = state.displayCard;
  if (!card) return "";
  return `
    <div class="display-overlay">
      <section class="display-card">
        <div class="display-title">${htmlEscape(card.title)}</div>
        <div class="display-primary">${nl(card.primary || "")}</div>
        ${card.subhead ? `<div class="display-subhead">${nl(card.subhead)}</div>` : ""}
        ${card.numbers ? `<div class="display-numbers">${card.numbers.map((n) => `<span>${htmlEscape(n)}</span>`).join("")}</div>` : ""}
        ${card.secondary ? `<div class="display-secondary">${nl(card.secondary)}</div>` : ""}
        ${card.footer ? `<div class="display-footer">${nl(card.footer)}</div>` : ""}
        <button class="ghost" data-action="close-display">${tr("closeDisplay")}</button>
      </section>
    </div>
  `;
}

function nl(text) {
  return htmlEscape(text).replaceAll("\n", "<br />");
}

function ctDistribution(count) {
  if (count === 5) return { townsfolk: 3, outsider: 0, minion: 1, demon: 1 };
  if (count === 6) return { townsfolk: 3, outsider: 1, minion: 1, demon: 1 };
  if (count === 7) return { townsfolk: 5, outsider: 0, minion: 1, demon: 1 };
  if (count === 8) return { townsfolk: 5, outsider: 1, minion: 1, demon: 1 };
  if (count === 9) return { townsfolk: 5, outsider: 2, minion: 1, demon: 1 };
  if (count === 10) return { townsfolk: 7, outsider: 0, minion: 2, demon: 1 };
  if (count === 11) return { townsfolk: 7, outsider: 1, minion: 2, demon: 1 };
  if (count === 12) return { townsfolk: 7, outsider: 2, minion: 2, demon: 1 };
  if (count === 13) return { townsfolk: 9, outsider: 0, minion: 3, demon: 1 };
  if (count === 14) return { townsfolk: 9, outsider: 1, minion: 3, demon: 1 };
  return { townsfolk: 9, outsider: 2, minion: 3, demon: 1 };
}

function startClocktower() {
  const count = state.seats.length;
  const dist = ctDistribution(count);
  const demon = getRole("imp");
  const minions = takeRandom(TB_ROLES.filter((r) => r.team === "minion"), dist.minion);
  const hasBaron = minions.some((r) => r.id === "baron");
  const outsiderCount = dist.outsider + (hasBaron ? 2 : 0);
  const townsfolkCount = Math.max(0, dist.townsfolk - (hasBaron ? 2 : 0));
  const outsiders = takeRandom(TB_ROLES.filter((r) => r.team === "outsider"), outsiderCount);
  const townsfolk = takeRandom(TB_ROLES.filter((r) => r.team === "townsfolk"), townsfolkCount);
  const actualRoles = shuffle([demon, ...minions, ...outsiders, ...townsfolk]);
  const actualIds = new Set(actualRoles.map((r) => r.id));
  const outOfPlayGood = TB_ROLES.filter((r) => ["townsfolk", "outsider"].includes(r.team) && !actualIds.has(r.id));
  const bluffs = takeRandom(outOfPlayGood, 3).map((r) => r.id);
  const fakeDrunkRoles = TB_ROLES.filter((r) => r.team === "townsfolk" && !actualIds.has(r.id));
  const players = state.seats.map((seat, index) => {
    const actual = actualRoles[index];
    const fake = actual.id === "drunk" ? (takeRandom(fakeDrunkRoles, 1)[0] || getRole("washerwoman")) : actual;
    return {
      id: seat.id,
      name: seat.name,
      seat: index + 1,
      actualRole: actual.id,
      shownRole: fake.id,
      alive: true,
    };
  });
  state.clocktower = {
    ...freshClocktower(),
    started: true,
    players,
    bluffs,
    log: [state.language === "en" ? "Game started." : "血染钟楼已开局。"],
  };
}

function getRole(id) {
  return TB_ROLES.find((r) => r.id === id);
}

function ctPlayers() {
  return state.clocktower.players;
}

function ctAlive() {
  return ctPlayers().filter((p) => p.alive);
}

function ctTeam(player) {
  return teamOfRole(player.actualRole);
}

function ctIsEvil(player) {
  return ["minion", "demon"].includes(ctTeam(player));
}

function ctSeatLabel(player) {
  return `${player.seat}${state.language === "en" ? "" : "号"} ${player.name}`;
}

function ctSeatLabelById(id) {
  const player = ctPlayers().find((p) => p.id === id);
  return player ? ctSeatLabel(player) : tr("noDeath");
}

function ctSeatNumberById(id) {
  const player = ctPlayers().find((p) => p.id === id);
  return player ? String(player.seat) : "";
}

function teamLabel(team) {
  const map = {
    townsfolk: state.language === "en" ? "Townsfolk" : "镇民",
    outsider: state.language === "en" ? "Outsider" : "外来者",
    minion: state.language === "en" ? "Minion" : "爪牙",
    demon: state.language === "en" ? "Demon" : "恶魔",
  };
  return map[team] || team;
}

function ctRoleActor(roleId) {
  return ctAlive().find((p) => p.actualRole === roleId);
}

function ctAnyRole(roleId) {
  return ctPlayers().find((p) => p.actualRole === roleId);
}

function ctMissingReason(roleId) {
  const role = ctAnyRole(roleId);
  if (!role) return state.language === "en" ? "This character is not in play." : "本局没有这个角色。";
  if (!role.alive) return state.language === "en" ? `${ctSeatLabel(role)} is dead and no longer acts.` : `${ctSeatLabel(role)} 已经死亡，死亡后不再执行这个能力。`;
  return "";
}

function ctNightSteps() {
  return state.clocktower.phase === "firstNight" ? ctFirstNightSteps() : ctLaterNightSteps();
}

function ctStep({ title, actor, real, reason, wakeText, actionText, tell, explain, control, display, headline, tip }) {
  return {
    title,
    actorId: actor ? actor.id : null,
    real,
    reason: reason || "",
    wakeText: wakeText || (actor ? `${state.language === "en" ? "Wake" : "请唤醒"} ${ctSeatLabel(actor)}。${ctSeatLabel(actor)} ${state.language === "en" ? "is" : "是"} ${title}。` : (state.language === "en" ? "Do not wake any player." : "不要唤醒任何玩家。")),
    actionText,
    tell,
    explain,
    control,
    display,
    headline,
    tip,
  };
}

function ctFirstNightSteps() {
  const c = state.clocktower;
  const demon = ctPlayers().find((p) => ctTeam(p) === "demon");
  const minions = ctPlayers().filter((p) => ctTeam(p) === "minion");
  const minionNames = minions.map(ctSeatLabel).join("、") || (state.language === "en" ? "none" : "无");
  const bluffNames = c.bluffs.map(nameOfRole).join(" / ");
  return [
    ctStep({
      title: state.language === "en" ? "Minion info" : "爪牙信息",
      actor: minions[0],
      real: minions.length > 0,
      reason: minions.length ? "" : (state.language === "en" ? "No Minions are in play." : "本局没有爪牙。"),
      wakeText: minions.length ? `${state.language === "en" ? "Wake all Minions" : "请唤醒所有爪牙"}：${minionNames}。` : "",
      headline: minions.length ? (state.language === "en" ? "Wake all Minions" : "唤醒所有爪牙") : (state.language === "en" ? "Minion placeholder" : "爪牙信息的占位操作"),
      actionText: state.language === "en"
        ? `Let the Minions recognize each other.\nTell them the Demon is ${demon ? ctSeatLabel(demon) : "unknown"}.\nSignal them to close their eyes.`
        : `示意爪牙互相确认。\n告诉他们恶魔是 ${demon ? ctSeatLabel(demon) : "未知"}。\n确认后示意他们闭眼。`,
      tell: demon ? `${state.language === "en" ? "The Demon is" : "恶魔是"}：\n${ctSeatLabel(demon)}` : null,
      explain: state.language === "en" ? "On the first night, Minions learn who the Demon is." : "首夜爪牙需要知道恶魔是谁，并确认彼此身份。",
      tip: state.language === "en" ? "Show the Demon seat, then close their eyes." : "展示恶魔座位，确认后让他们闭眼。",
      display: demon ? { title: state.language === "en" ? "Minion info" : "爪牙信息", primary: state.language === "en" ? "Demon" : "恶魔", secondary: ctSeatLabel(demon), footer: state.language === "en" ? "This player is the Demon." : "这名玩家是恶魔。" } : null,
    }),
    ctStep({
      title: state.language === "en" ? "Demon info" : "恶魔信息",
      actor: demon,
      real: !!demon,
      reason: demon ? "" : (state.language === "en" ? "There is no Demon right now." : "当前没有恶魔。"),
      headline: demon ? (state.language === "en" ? `Wake ${ctSeatLabel(demon)}` : `唤醒 ${ctSeatLabel(demon)}`) : (state.language === "en" ? "Demon placeholder" : "恶魔信息的占位操作"),
      actionText: state.language === "en"
        ? `Tell the Demon who the Minions are.\nTell the Demon the three bluff characters.\nThe Demon does not kill on night 1.`
        : `告诉恶魔爪牙是谁。\n告诉恶魔本局可用伪装身份。\n首夜恶魔不进行击杀。`,
      tell: `${state.language === "en" ? "Minions" : "爪牙"}：${minionNames}\n${state.language === "en" ? "Bluffs" : "可用伪装身份"}：${bluffNames}`,
      explain: state.language === "en" ? "The Demon receives Minion info and bluffs, but does not kill on night 1." : "首夜恶魔需要知道爪牙是谁，并获得 3 个伪装身份。首夜不进行击杀。",
      tip: state.language === "en" ? "Show Minions and bluffs. No kill tonight." : "展示爪牙和伪装身份，首夜不杀人。",
      display: demon ? { title: state.language === "en" ? "Demon info" : "恶魔信息", primary: state.language === "en" ? "Minions" : "爪牙", secondary: `${minionNames}\n\n${state.language === "en" ? "Bluffs" : "伪装"}：${bluffNames}`, footer: state.language === "en" ? "No kill on night 1." : "首夜不进行击杀。" } : null,
    }),
    ctRedHerringStep(),
    ctWasherwomanStep(),
    ctLibrarianStep(),
    ctInvestigatorStep(),
    ctChefStep(),
    ctEmpathStep(),
    ctFortuneTellerStep(),
    ctButlerStep(),
    ctSpyStep(),
  ];
}

function ctLaterNightSteps() {
  return [
    ctPoisonerStep(),
    ctUndertakerStep(),
    ctButlerStep(),
    ctEmpathStep(),
    ctFortuneTellerStep(),
    ctDemonKillStep(),
    ctRavenkeeperStep(),
  ];
}

function ctInfoStep(roleId, tell, explain, display, control, options = {}) {
  const actor = ctRoleActor(roleId);
  const title = nameOfRole(roleId);
  return ctStep({
    title,
    actor,
    real: !!actor,
    reason: ctMissingReason(roleId),
    actionText: actor
      ? (state.language === "en" ? `Wake ${ctSeatLabel(actor)}. Give the information, then signal them to close their eyes.` : `轻拍 ${ctSeatLabel(actor)}，示意他睁眼。\n给出今晚的信息。\n确认后示意他闭眼。`)
      : (state.language === "en" ? "Pause briefly to preserve the night rhythm, then continue." : "为了避免泄露信息，请停顿 2-3 秒，然后点击下一步。"),
    tell: actor ? tell : null,
    explain,
    display: actor && display ? display : null,
    control,
    headline: options.headline,
    tip: options.tip,
  });
}

function ctRedHerringStep() {
  const actor = ctAnyRole("fortuneTeller");
  return ctStep({
    title: state.language === "en" ? "Fortune Teller red herring" : "占卜师红鲱鱼",
    actor: null,
    real: !!actor,
    reason: actor ? "" : (state.language === "en" ? "No Fortune Teller is in play." : "本局没有占卜师，此步骤只用于首夜配置。"),
    wakeText: state.language === "en" ? "Do not wake any player." : "不要唤醒任何玩家。",
    headline: actor ? (state.language === "en" ? "Choose the red herring" : "选择占卜师红鲱鱼") : (state.language === "en" ? "Fortune Teller placeholder" : "占卜师红鲱鱼的占位操作"),
    actionText: state.language === "en" ? "Choose a good player who may register as a Demon to the Fortune Teller." : "请选择一名好人玩家作为占卜师可能得到“是”的红鲱鱼。这个信息只给说书人看。",
    tell: state.clocktower.redHerring ? `${state.language === "en" ? "Selected" : "已选择"}：${ctSeatLabelById(state.clocktower.redHerring)}` : null,
    explain: state.language === "en" ? "This is private Storyteller setup. Do not show it to players." : "红鲱鱼是占卜师规则的一部分。不要公开给玩家。",
    tip: state.language === "en" ? "Private setup only. Do not show players." : "只给说书人看，不公开。",
    control: "redHerring",
  });
}

function ctWasherwomanStep() {
  const target = ctPlayers().find((p) => teamOfRole(p.actualRole) === "townsfolk" && p.actualRole !== "washerwoman");
  const pair = target ? ctPair(target) : null;
  const roleName = target ? nameOfRole(target.actualRole) : "";
  const tell = target && pair ? `${roleName}\n${ctSeatLabel(target)} / ${ctSeatLabel(pair)}` : null;
  return ctInfoStep("washerwoman", tell, state.language === "en" ? "The Washerwoman learns one of two players is a specific Townsfolk." : "洗衣妇会得知某个镇民在两名玩家之一中。", target && pair ? { title: state.language === "en" ? "Washerwoman info" : "洗衣妇信息", primary: roleName, subhead: state.language === "en" ? "is one of these two players" : "在下面两位玩家之中", numbers: [target.seat, pair.seat], footer: "" } : null);
}

function ctLibrarianStep() {
  const target = ctPlayers().find((p) => teamOfRole(p.actualRole) === "outsider");
  const pair = target ? ctPair(target) : null;
  const roleName = target ? nameOfRole(target.actualRole) : "";
  const tell = target && pair ? `${roleName}\n${ctSeatLabel(target)} / ${ctSeatLabel(pair)}` : (state.language === "en" ? "No Outsiders." : "没有异乡人。");
  return ctInfoStep("librarian", tell, state.language === "en" ? "The Librarian learns an Outsider is one of two players, or that there are no Outsiders." : "图书管理员会得知某个异乡人在两名玩家之一中，或得知没有异乡人。", target && pair ? { title: state.language === "en" ? "Librarian info" : "图书管理员信息", primary: roleName, subhead: state.language === "en" ? "is one of these two players" : "在下面两位玩家之中", numbers: [target.seat, pair.seat], footer: "" } : { title: state.language === "en" ? "Librarian info" : "图书管理员信息", primary: state.language === "en" ? "No Outsiders" : "没有异乡人", footer: "" });
}

function ctInvestigatorStep() {
  const target = ctPlayers().find((p) => teamOfRole(p.actualRole) === "minion");
  const pair = target ? ctPair(target) : null;
  const roleName = target ? nameOfRole(target.actualRole) : "";
  const tell = target && pair ? `${roleName}\n${ctSeatLabel(target)} / ${ctSeatLabel(pair)}` : (state.language === "en" ? "No Minions." : "没有爪牙。");
  return ctInfoStep("investigator", tell, state.language === "en" ? "The Investigator learns a Minion is one of two players, or that there are no Minions." : "调查员会得知某个爪牙在两名玩家之一中，或得知没有爪牙。", target && pair ? { title: state.language === "en" ? "Investigator info" : "调查员信息", primary: roleName, subhead: state.language === "en" ? "is one of these two players" : "在下面两位玩家之中", numbers: [target.seat, pair.seat], footer: "" } : { title: state.language === "en" ? "Investigator info" : "调查员信息", primary: state.language === "en" ? "No Minions" : "没有爪牙", footer: "" });
}

function ctChefStep() {
  const num = String(ctChefPairs());
  return ctInfoStep("chef", num, state.language === "en" ? "This number is how many pairs of evil players are sitting next to each other." : "这个数字表示有几对邪恶玩家相邻而坐。", { title: state.language === "en" ? "Chef info" : "厨师信息", primary: num, footer: state.language === "en" ? `There are ${num} adjacent evil pairs.` : `有 ${num} 对邪恶玩家相邻。` });
}

function ctEmpathStep() {
  const actor = ctRoleActor("empath");
  const num = actor ? String(ctLivingNeighbors(actor).filter(ctIsEvil).length) : "";
  return ctInfoStep("empath", num, state.language === "en" ? "This number is how many of the Empath's two living neighbors are evil." : "这个数字表示共情者两个存活邻居中有几个邪恶玩家。", actor ? { title: state.language === "en" ? "Empath info" : "共情者信息", primary: num, footer: state.language === "en" ? `Your two living neighbors include ${num} evil players.` : `你的两个存活邻居中有 ${num} 个邪恶玩家。` } : null);
}

function ctFortuneTellerStep() {
  const actor = ctRoleActor("fortuneTeller");
  return ctInfoStep("fortuneTeller", null, state.language === "en" ? "If either chosen player is the Demon or red herring, answer Yes." : "让占卜师选择两名玩家。如果其中有恶魔或红鲱鱼，告诉他“是”。", null, "fortune", {
    headline: actor ? (state.language === "en" ? `Wake ${ctSeatLabel(actor)}` : `唤醒占卜师：${ctSeatLabel(actor)}`) : undefined,
    tip: state.language === "en" ? "Let them point at two seats. Tap Check and show." : "让他选择两名玩家，点查询后直接展示结果。",
  });
}

function ctFortuneDisplayCard() {
  const result = ctFortuneResult() ? (state.language === "en" ? "Yes" : "是") : (state.language === "en" ? "No" : "否");
  return {
    title: state.language === "en" ? "Fortune Teller" : "占卜师",
    primary: result,
    subhead: state.language === "en" ? "for these two players" : "查询这两名玩家",
    numbers: [ctSeatNumberById(state.clocktower.fortuneFirst), ctSeatNumberById(state.clocktower.fortuneSecond)].filter(Boolean),
    footer: "",
  };
}

function ctButlerStep() {
  const c = state.clocktower;
  const tell = c.butlerMaster ? `${state.language === "en" ? "Today's master" : "今天的主人"}：${ctSeatLabelById(c.butlerMaster)}` : null;
  return ctInfoStep("butler", tell, state.language === "en" ? "The Butler chooses a master. During the day, they may vote only if their master votes." : "管家每天选择一名主人，白天只能在主人投票时投票。", tell ? { title: state.language === "en" ? "Butler info" : "管家信息", primary: ctSeatLabelById(c.butlerMaster), footer: state.language === "en" ? "This is your master today." : "这是你今天的主人。" } : null, "butlerMaster");
}

function ctSpyStep() {
  const actor = ctRoleActor("spy");
  return ctStep({
    title: nameOfRole("spy"),
    actor,
    real: !!actor,
    reason: ctMissingReason("spy"),
    actionText: actor ? (state.language === "en" ? "Let the Spy inspect the full grimoire, then signal them to close their eyes." : "让间谍查看完整魔典，确认后示意他闭眼。") : (state.language === "en" ? "Pause briefly, then continue." : "停顿 2-3 秒，然后点击下一步。"),
    explain: state.language === "en" ? "The Spy may see all true identities." : "间谍可以查看所有玩家的真实身份。",
  });
}

function ctPoisonerStep() {
  return ctInfoStep("poisoner", state.clocktower.poisonTarget ? `${state.language === "en" ? "Selected" : "已选择"}：${ctSeatLabelById(state.clocktower.poisonTarget)}` : null, state.language === "en" ? "The Poisoner chooses a player whose ability temporarily stops working." : "投毒者选择一名玩家，使其能力暂时失效。", null, "poisonTarget");
}

function ctUndertakerStep() {
  const executed = state.clocktower.lastExecuted ? ctPlayers().find((p) => p.id === state.clocktower.lastExecuted) : null;
  const tell = executed ? `${ctSeatLabel(executed)} ${state.language === "en" ? "was" : "的角色是"}：\n${nameOfRole(executed.actualRole)}` : null;
  return ctInfoStep("undertaker", tell, state.language === "en" ? "The Undertaker learns the character of the player executed today." : "送葬者会得知今天被处决玩家的角色。", executed ? { title: state.language === "en" ? "Undertaker info" : "送葬者信息", primary: ctSeatLabel(executed), secondary: nameOfRole(executed.actualRole), footer: state.language === "en" ? "This was the executed player's character." : "这是被处决玩家的角色。" } : null);
}

function ctDemonKillStep() {
  const demon = ctAlive().find((p) => ctTeam(p) === "demon");
  const demonPoisoned = ctDemonPoisonedTonight();
  return ctStep({
    title: state.language === "en" ? "Demon action" : "恶魔行动",
    actor: demon,
    real: !!demon,
    reason: demon ? "" : (state.language === "en" ? "There is no living Demon." : "当前没有存活恶魔。"),
    actionText: demon ? (state.language === "en" ? "Wake the Demon. Let them choose tonight's death target. Record it, but do not announce now." : `轻拍 ${ctSeatLabel(demon)}，示意当前恶魔睁眼。\n让他选择今晚要杀死的玩家。\n在下方记录目标，但不要现在宣布死亡。`) : (state.language === "en" ? "Pause briefly, then continue." : "不要唤醒任何玩家，停顿 2-3 秒后继续。"),
    tell: demonPoisoned
      ? (state.language === "en" ? "The Demon is poisoned. Tonight's kill will fail." : "恶魔已中毒，今晚杀人会失效。")
      : (state.clocktower.pendingNightDeath ? `${state.language === "en" ? "Recorded" : "已记录"}：${ctSeatLabelById(state.clocktower.pendingNightDeath)}` : null),
    explain: demonPoisoned
      ? (state.language === "en" ? "You may record the Demon choice, but no one dies from it at dawn." : "可以记录恶魔选择，但天亮不会因此死亡。")
      : (state.language === "en" ? "Announce deaths only at dawn." : "恶魔选择的死亡目标会在天亮时统一宣布。"),
    control: "nightDeath",
  });
}

function ctRavenkeeperStep() {
  const death = !ctDemonPoisonedTonight() && state.clocktower.pendingNightDeath ? ctPlayers().find((p) => p.id === state.clocktower.pendingNightDeath) : null;
  const actor = death && death.actualRole === "ravenkeeper" ? death : null;
  const target = state.clocktower.ravenkeeperTarget ? ctPlayers().find((p) => p.id === state.clocktower.ravenkeeperTarget) : null;
  const tell = target ? `${ctSeatLabel(target)} ${state.language === "en" ? "is" : "的角色是"}：\n${nameOfRole(target.actualRole)}` : null;
  return ctStep({
    title: nameOfRole("ravenkeeper"),
    actor,
    real: !!actor,
    reason: actor ? "" : (state.language === "en" ? "The Ravenkeeper did not die tonight." : "守鸦人今晚没有死亡，不需要唤醒。"),
    actionText: actor ? (state.language === "en" ? "Wake the Ravenkeeper. Let them choose a player and show that character." : "唤醒守鸦人。让他选择一名玩家，并告诉他该玩家的角色。") : (state.language === "en" ? "Pause briefly, then continue." : "为了保持夜晚节奏，请停顿 2-3 秒。"),
    tell,
    explain: state.language === "en" ? "Only wake the Ravenkeeper if they died at night." : "只有守鸦人夜晚死亡时才唤醒他。",
    control: "ravenkeeperTarget",
    display: target ? { title: state.language === "en" ? "Ravenkeeper info" : "守鸦人信息", primary: ctSeatLabel(target), secondary: nameOfRole(target.actualRole), footer: state.language === "en" ? "This player's character." : "该玩家的角色。" } : null,
  });
}

function ctDemonPoisonedTonight() {
  const poisonTarget = state.clocktower.poisonTarget ? ctPlayers().find((p) => p.id === state.clocktower.poisonTarget) : null;
  return !!poisonTarget && poisonTarget.alive && ctTeam(poisonTarget) === "demon";
}

function ctPair(target) {
  const players = ctPlayers().filter((p) => p.id !== target.id);
  const index = players.findIndex((p) => p.seat > target.seat);
  return players[index >= 0 ? index : 0];
}

function ctChefPairs() {
  const players = ctPlayers().sort((a, b) => a.seat - b.seat);
  let pairs = 0;
  for (let i = 0; i < players.length; i += 1) {
    const current = players[i];
    const next = players[(i + 1) % players.length];
    if (ctIsEvil(current) && ctIsEvil(next)) pairs += 1;
  }
  return pairs;
}

function ctLivingNeighbors(player) {
  const players = ctPlayers().sort((a, b) => a.seat - b.seat);
  const index = players.findIndex((p) => p.id === player.id);
  const left = findLivingNeighbor(players, index, -1);
  const right = findLivingNeighbor(players, index, 1);
  return [left, right].filter(Boolean);
}

function findLivingNeighbor(players, startIndex, direction) {
  for (let offset = 1; offset < players.length; offset += 1) {
    const next = players[(startIndex + direction * offset + players.length) % players.length];
    if (next.alive) return next;
  }
  return null;
}

function ctFortuneResult() {
  const c = state.clocktower;
  const targets = [c.fortuneFirst, c.fortuneSecond].map((id) => ctPlayers().find((p) => p.id === id)).filter(Boolean);
  return targets.some((p) => ctTeam(p) === "demon" || p.id === c.redHerring);
}

function finishClocktowerNight() {
  const c = state.clocktower;
  c.nightStarted = false;
  c.nightIndex = 0;
  if (c.phase === "firstNight") {
    c.lastNightDeath = null;
    c.phase = "dawn";
    return;
  }
  const demonPoisoned = ctDemonPoisonedTonight();
  c.lastNightDeath = demonPoisoned ? null : c.pendingNightDeath;
  if (!demonPoisoned && c.pendingNightDeath) {
    const target = ctPlayers().find((p) => p.id === c.pendingNightDeath);
    if (target && target.alive) ctKill(target, state.language === "en" ? "night death" : "夜晚死亡");
  }
  c.poisonTarget = null;
  c.fortuneFirst = null;
  c.fortuneSecond = null;
  c.ravenkeeperTarget = null;
  if (!c.outcome) c.phase = "dawn";
}

function ctKill(player, cause) {
  const c = state.clocktower;
  player.alive = false;
  c.log.unshift(`${ctSeatLabel(player)}：${cause}`);
  if (ctTeam(player) === "demon") {
    const aliveAfter = ctAlive().length;
    const scarlet = ctAlive().find((p) => p.actualRole === "scarletWoman");
    if (scarlet && aliveAfter >= 5) {
      scarlet.actualRole = "imp";
      c.log.unshift(`${ctSeatLabel(scarlet)} ${state.language === "en" ? "became the new Demon." : "接替成为新的恶魔。"}`);
      return;
    }
  }
  ctCheckOutcome();
}

function ctCheckOutcome() {
  const c = state.clocktower;
  const alive = ctAlive();
  const hasDemon = alive.some((p) => ctTeam(p) === "demon");
  if (!hasDemon) {
    c.outcome = { winner: state.language === "en" ? "Good wins" : "善良阵营获胜", reason: state.language === "en" ? "There is no living Demon." : "当前没有存活的恶魔。" };
  } else if (alive.length <= 2) {
    c.outcome = { winner: state.language === "en" ? "Evil wins" : "邪恶阵营获胜", reason: state.language === "en" ? "Only two players live and the Demon still lives." : "只剩 2 名玩家且恶魔仍然存活。" };
  }
}

function confirmClocktowerDay() {
  const c = state.clocktower;
  const threshold = Math.floor(ctAlive().length / 2) + 1;
  const targetId = c.highestVoteCount >= threshold ? c.highestVoteName : null;
  if (!targetId) {
    if (ctAlive().length === 3 && ctAlive().some((p) => p.actualRole === "mayor")) {
      c.outcome = { winner: state.language === "en" ? "Good wins" : "善良阵营获胜", reason: state.language === "en" ? "Mayor win condition: three alive and no execution." : "市长条件：只剩三名玩家且今天无人被处决。" };
      return;
    }
    startNextClocktowerNight();
    return;
  }
  const target = ctPlayers().find((p) => p.id === targetId);
  c.lastExecuted = targetId;
  if (target && target.alive) {
    if (target.actualRole === "saint") {
      target.alive = false;
      c.outcome = { winner: state.language === "en" ? "Evil wins" : "邪恶阵营获胜", reason: state.language === "en" ? "The Saint was executed." : "圣徒被处决，善良阵营失败。" };
      return;
    }
    ctKill(target, state.language === "en" ? "executed" : "被处决");
  }
  if (!c.outcome) startNextClocktowerNight();
}

function startNextClocktowerNight() {
  const c = state.clocktower;
  c.round += 1;
  c.phase = "night";
  c.dayMode = "overview";
  c.nominator = null;
  c.nominee = null;
  c.voteCount = 0;
  c.highestVoteName = null;
  c.highestVoteCount = 0;
  c.selectedExecution = null;
  c.pendingNightDeath = null;
  c.lastNightDeath = null;
}

function fireSlayer() {
  const c = state.clocktower;
  const target = ctPlayers().find((p) => p.id === c.slayerTarget);
  c.slayerUsed = true;
  if (target && ctTeam(target) === "demon") {
    ctKill(target, state.language === "en" ? "shot by Slayer" : "被猎手击杀");
  } else if (target) {
    c.log.unshift(`${ctSeatLabel(target)} ${state.language === "en" ? "was not the Demon. No one died." : "不是恶魔，没有玩家死亡。"}`);
  }
  c.slayerTarget = null;
}

function startWerewolf() {
  const count = state.seats.length;
  const wolves = Math.max(1, Math.floor(count / 4));
  const roleList = [
    ...Array(wolves).fill({ role: state.language === "en" ? "Werewolf" : "狼人", team: "evil" }),
    { role: state.language === "en" ? "Seer" : "预言家", team: "good" },
    { role: state.language === "en" ? "Witch" : "女巫", team: "good" },
    { role: state.language === "en" ? "Hunter" : "猎人", team: "good" },
  ];
  while (roleList.length < count) roleList.push({ role: state.language === "en" ? "Villager" : "村民", team: "good" });
  const roles = shuffle(roleList).map((role, index) => ({ ...role, id: state.seats[index].id, name: state.seats[index].name, seat: index + 1 }));
  state.werewolf = { ...freshWerewolf(), started: true, roles };
}

function werewolfSteps() {
  return [
    { title: state.language === "en" ? "Werewolves act" : "狼人行动", wake: state.language === "en" ? "Wake all Werewolves." : "请唤醒所有狼人。", action: state.language === "en" ? "Let them choose tonight's kill target. Record it only." : "让狼人选择今晚击杀目标。只记录，不要现在宣布。", field: "nightDeath" },
    { title: state.language === "en" ? "Seer checks" : "预言家查验", wake: state.language === "en" ? "Wake the Seer." : "请唤醒预言家。", action: state.language === "en" ? "Let the Seer choose one player. Tell them good or evil." : "让预言家选择一名玩家，告诉他好人或狼人。", field: "checkedPlayer" },
    { title: state.language === "en" ? "Witch acts" : "女巫行动", wake: state.language === "en" ? "Wake the Witch." : "请唤醒女巫。", action: state.language === "en" ? "Tell the Witch who died. Let them save or poison if available." : "告诉女巫今晚死亡目标，让她选择是否救人或毒人。", field: "poisonTarget" },
    { title: state.language === "en" ? "Dawn" : "天亮", wake: state.language === "en" ? "Wake everyone." : "所有人睁眼。", action: state.language === "en" ? "Announce last night's death, then enter discussion." : "宣布昨晚死亡，然后进入白天讨论。" },
  ];
}

function startUndercover() {
  const u = state.undercover;
  u.civilianWord = document.querySelector("#civilianWord")?.value.trim() || u.civilianWord;
  u.undercoverWord = document.querySelector("#undercoverWord")?.value.trim() || u.undercoverWord;
  u.undercoverCount = Math.max(1, Number(document.querySelector("#undercoverCount")?.value || 1));
  u.blankCount = Math.max(0, Number(document.querySelector("#blankCount")?.value || 0));
  const roles = [
    ...Array(u.undercoverCount).fill("undercover"),
    ...Array(u.blankCount).fill("blank"),
  ];
  while (roles.length < state.seats.length) roles.push("civilian");
  const shuffled = shuffle(roles).slice(0, state.seats.length);
  u.assignments = state.seats.map((seat, index) => ({
    id: seat.id,
    name: seat.name,
    seat: index + 1,
    role: shuffled[index],
    word: shuffled[index] === "civilian" ? u.civilianWord : shuffled[index] === "undercover" ? u.undercoverWord : state.language === "en" ? "Blank" : "白板",
  }));
  u.started = true;
}

document.addEventListener("click", (event) => {
  const target = event.target.closest("[data-action]");
  if (!target) return;
  const action = target.dataset.action;

  if (action === "set-language") state.language = target.dataset.lang;
  if (action === "add-common") {
    const input = document.querySelector("#commonName");
    const name = input.value.trim();
    if (name && !state.commonPlayers.some((p) => p.name === name)) state.commonPlayers.push({ id: uid(), name });
  }
  if (action === "add-temp") {
    const input = document.querySelector("#tempName");
    const name = input.value.trim();
    if (name) state.seats.push({ id: uid(), name, type: "temp" });
  }
  if (action === "add-common-seat") {
    const player = state.commonPlayers[Number(target.dataset.index)];
    if (player && !state.seats.some((p) => p.name === player.name)) state.seats.push({ id: uid(), name: player.name, type: "common" });
  }
  if (action === "remove-seat") state.seats = state.seats.filter((p) => p.id !== target.dataset.id);
  if (action === "move-seat") moveSeat(target.dataset.id, Number(target.dataset.dir));
  if (action === "clear-seats") state.seats = [];
  if (action === "select-game") state.selectedGame = target.dataset.game;
  if (action === "start-clocktower") startClocktower();
  if (action === "reset-clocktower") state.clocktower = freshClocktower();
  if (action === "ct-start-night") state.clocktower.nightStarted = true;
  if (action === "ct-next-night") {
    const c = state.clocktower;
    const steps = ctNightSteps();
    if (c.nightIndex < steps.length - 1) c.nightIndex += 1;
    else finishClocktowerNight();
  }
  if (action === "ct-enter-day") {
    state.clocktower.phase = "day";
    state.clocktower.lastNightDeath = null;
    state.clocktower.pendingNightDeath = null;
  }
  if (action === "ct-select") {
    const field = target.dataset.field;
    const id = target.dataset.id;
    state.clocktower[field] = state.clocktower[field] === id ? null : id;
    if (field === "fortuneFirst" && state.clocktower.fortuneSecond === state.clocktower.fortuneFirst) {
      state.clocktower.fortuneSecond = null;
    }
  }
  if (action === "ct-day-mode") {
    state.clocktower.dayMode = target.dataset.mode;
    if (target.dataset.mode === "vote") state.clocktower.voteCount = Math.floor(ctAlive().length / 2) + 1;
  }
  if (action === "ct-vote-minus") state.clocktower.voteCount = Math.max(0, state.clocktower.voteCount - 1);
  if (action === "ct-vote-plus") state.clocktower.voteCount = Math.min(ctAlive().length, state.clocktower.voteCount + 1);
  if (action === "ct-save-vote" || action === "ct-end-after-vote") {
    const c = state.clocktower;
    const threshold = Math.floor(ctAlive().length / 2) + 1;
    if (c.voteCount >= threshold && c.voteCount >= c.highestVoteCount) {
      c.highestVoteName = c.nominee;
      c.highestVoteCount = c.voteCount;
    }
    c.dayMode = action === "ct-save-vote" ? "overview" : "endConfirm";
  }
  if (action === "ct-confirm-day") confirmClocktowerDay();
  if (action === "ct-fire-slayer") fireSlayer();
  if (action === "show-display") state.displayCard = JSON.parse(decodeURIComponent(target.dataset.card));
  if (action === "ct-show-fortune") state.displayCard = ctFortuneDisplayCard();
  if (action === "close-display") state.displayCard = null;
  if (action === "start-werewolf") startWerewolf();
  if (action === "werewolf-select") state.werewolf[target.dataset.field] = state.werewolf[target.dataset.field] === target.dataset.id ? null : target.dataset.id;
  if (action === "werewolf-next") {
    const w = state.werewolf;
    const steps = werewolfSteps();
    if (w.stepIndex < steps.length - 1) w.stepIndex += 1;
    else {
      w.stepIndex = 0;
      w.phase = w.phase === "night" ? "day" : "night";
      if (w.phase === "night") w.round += 1;
    }
  }
  if (action === "start-undercover") startUndercover();
  if (action === "reset-undercover") state.undercover = freshUndercover();
  if (action === "undercover-reveal") {
    const assignment = state.undercover.assignments.find((a) => a.id === target.dataset.id);
    if (assignment) {
      state.displayCard = {
        title: `${assignment.seat}号 ${assignment.name}`,
        primary: assignment.word,
        footer: assignment.role === "undercover" ? (state.language === "en" ? "You are undercover." : "你是卧底。") : assignment.role === "blank" ? (state.language === "en" ? "You are blank." : "你是白板。") : (state.language === "en" ? "Remember your word." : "记住你的词语。"),
      };
    }
  }
  if (action === "export-state") exportBackup();

  saveState();
  render();
});

function moveSeat(id, dir) {
  const index = state.seats.findIndex((p) => p.id === id);
  const next = index + dir;
  if (index < 0 || next < 0 || next >= state.seats.length) return;
  [state.seats[index], state.seats[next]] = [state.seats[next], state.seats[index]];
}

function exportBackup() {
  const data = JSON.stringify(state, null, 2);
  navigator.clipboard?.writeText(data);
  alert(state.language === "en" ? "Backup JSON copied to clipboard." : "备份 JSON 已复制到剪贴板。");
}

if ("serviceWorker" in navigator) {
  window.addEventListener("load", () => {
    navigator.serviceWorker.register("./sw.js").catch(() => {});
  });
}

render();
