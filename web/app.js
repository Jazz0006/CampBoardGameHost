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

const CLOCKTOWER_SCRIPTS = [
  { id: "noGreaterJoy", zh: "No Greater Joy", en: "No Greater Joy", enabled: true },
  { id: "troubleBrewing", zh: "暗流涌动", en: "Trouble Brewing", enabled: true },
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
  ["clockmaker", "townsfolk", "钟表匠", "Clockmaker"],
  ["chambermaid", "townsfolk", "侍女", "Chambermaid"],
  ["artist", "townsfolk", "艺术家", "Artist"],
  ["sage", "townsfolk", "贤者", "Sage"],
  ["butler", "outsider", "管家", "Butler"],
  ["drunk", "outsider", "酒鬼", "Drunk"],
  ["klutz", "outsider", "呆瓜", "Klutz"],
  ["recluse", "outsider", "隐士", "Recluse"],
  ["saint", "outsider", "圣徒", "Saint"],
  ["poisoner", "minion", "投毒者", "Poisoner"],
  ["spy", "minion", "间谍", "Spy"],
  ["baron", "minion", "男爵", "Baron"],
  ["scarletWoman", "minion", "猩红女郎", "Scarlet Woman"],
  ["imp", "demon", "小恶魔", "Imp"],
].map(([id, team, zh, en]) => ({ id, team, zh, en }));

const TROUBLE_BREWING_ROLE_IDS = [
  "washerwoman", "librarian", "investigator", "chef", "empath", "fortuneTeller",
  "undertaker", "monk", "ravenkeeper", "virgin", "slayer", "soldier", "mayor",
  "butler", "drunk", "recluse", "saint",
  "poisoner", "spy", "baron", "scarletWoman", "imp",
];

const NO_GREATER_JOY_ROLE_IDS = [
  "clockmaker", "investigator", "empath", "chambermaid", "artist", "sage",
  "drunk", "klutz", "baron", "scarletWoman", "imp",
];

const state = loadState();

function defaultState() {
  return {
    language: "zh",
    selectedGame: "clocktower",
    selectedClocktowerScript: null,
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
    chambermaidFirst: null,
    chambermaidSecond: null,
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
    highestVoteNames: [],
    selectedExecution: null,
    pendingKlutz: null,
    klutzChoice: null,
    klutzReturnToDawn: false,
    slayerTarget: null,
    slayerClaimant: null,
    slayerClaimantsUsed: [],
    slayerUsed: false,
    virginUsed: false,
    artistClaimant: null,
    artistClaimantsUsed: [],
    artistUsed: false,
    outcome: null,
    grimoireOpen: true,
    testMode: false,
    testLabOpen: false,
    testNotice: "",
    eventCounter: 0,
    events: [],
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
    return saved ? normalizeState(saved) : defaultState();
  } catch {
    return defaultState();
  }
}

function normalizeState(saved) {
  const base = defaultState();
  const clocktower = {
    ...freshClocktower(),
    ...(saved.clocktower || {}),
  };
  const legacyLog = Array.isArray(clocktower.log) ? clocktower.log : [];
  const savedEvents = Array.isArray(clocktower.events) ? clocktower.events : [];
  clocktower.events = savedEvents.length
    ? savedEvents.map((event, index) => ({
      id: event.id || uid(),
      sequence: Number(event.sequence || index + 1),
      createdAt: event.createdAt || new Date().toISOString(),
      phase: event.phase || "system",
      round: Number(event.round || 1),
      type: event.type || "system",
      title: event.title || (saved.language === "en" ? "Record" : "记录"),
      detail: event.detail || "",
      playerIds: Array.isArray(event.playerIds) ? event.playerIds : [],
      visibility: event.visibility || "storyteller",
      meta: event.meta || {},
    }))
    : legacyLog.slice().reverse().map((detail, index) => legacyLogEvent(detail, index + 1, saved.language));
  clocktower.eventCounter = Math.max(
    Number(clocktower.eventCounter || 0),
    ...clocktower.events.map((event) => Number(event.sequence || 0)),
  );
  clocktower.grimoireOpen = clocktower.grimoireOpen !== false;
  return {
    ...base,
    ...saved,
    clocktower,
    werewolf: { ...freshWerewolf(), ...(saved.werewolf || {}) },
    undercover: { ...freshUndercover(), ...(saved.undercover || {}) },
  };
}

function legacyLogEvent(detail, sequence, language = "zh") {
  return {
    id: uid(),
    sequence,
    createdAt: new Date().toISOString(),
    phase: "legacy",
    round: 1,
    type: "legacy",
    title: language === "en" ? "Imported note" : "旧记录",
    detail,
    playerIds: [],
    visibility: "storyteller",
    meta: {},
  };
}

function ctAddEvent({ type, title, detail, playerIds = [], phase, round, visibility = "storyteller", meta = {} }) {
  const c = state.clocktower;
  c.eventCounter = Number(c.eventCounter || 0) + 1;
  const event = {
    id: uid(),
    sequence: c.eventCounter,
    createdAt: new Date().toISOString(),
    phase: phase || c.phase || "system",
    round: Number(round || c.round || 1),
    type,
    title,
    detail,
    playerIds,
    visibility,
    meta,
  };
  c.events = Array.isArray(c.events) ? c.events : [];
  c.events.push(event);
  c.log = Array.isArray(c.log) ? c.log : [];
  if (detail) c.log.unshift(detail);
  return event;
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
  const role = getRole(roleId);
  if (!role) return roleId || "";
  return state.language === "en" ? role.en : role.zh;
}

function teamOfRole(roleId) {
  return (getRole(roleId) || {}).team;
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
    const scriptId = effectiveClocktowerScript();
    const showScriptChoice = state.seats.length === 5 || state.seats.length === 6;
    const canStart = state.seats.length >= 5 && canStartClocktowerScript(scriptId);
    return `
      <div class="stack">
        <div class="instruction">
          <strong>${state.language === "en" ? "Clocktower script" : "血染钟楼剧本"}：${clocktowerScriptName(scriptId)}</strong>
          ${state.language === "en"
            ? (showScriptChoice ? "For 5-6 players, No Greater Joy is selected by default. You can switch to Trouble Brewing." : "For 7+ players, Trouble Brewing is used automatically.")
            : (showScriptChoice ? "5–6 人默认选择 No Greater Joy。可切换到暗流涌动。" : "7 人及以上自动使用暗流涌动。")}
        </div>
        ${showScriptChoice ? `
          <div class="game-pills">
            ${CLOCKTOWER_SCRIPTS.map((script) => `
              <button class="pill ${scriptId === script.id ? "active" : ""}" data-action="select-clocktower-script" data-script="${script.id}">
                ${clocktowerScriptName(script.id)}
              </button>
            `).join("")}
          </div>
        ` : ""}
        ${scriptId === "noGreaterJoy" ? `<div class="step-note">${state.language === "en" ? "Default for 5-6 players. You can switch back to Trouble Brewing." : "5–6 人默认使用 No Greater Joy；可切换回暗流涌动。"}</div>` : ""}
        <button class="primary" data-action="start-clocktower" ${canStart ? "" : "disabled"}>
          ${state.seats.length < 5
            ? `${tr("start")} (${state.language === "en" ? "needs 5+" : "至少 5 人"})`
            : tr("start")}
        </button>
        <div class="test-lab-entry">
          <strong>${state.language === "en" ? "Developer shortcut" : "开发测试快捷入口"}</strong>
          <span>${state.language === "en" ? "Create fixed seats and jump directly to any character or poison scenario." : "自动创建固定座位，直接跳到任意角色或中毒场景。"}</span>
          <button class="ghost" data-action="ct-test-start">${state.language === "en" ? "Open test lab" : "打开测试实验室"}</button>
        </div>
      </div>
    `;
  }

  return `
    <div class="stack">
      ${renderClocktowerSummary()}
      ${c.testMode ? renderClocktowerTestLab() : ""}
      ${c.outcome ? renderClocktowerOutcome() : renderClocktowerPhase()}
      ${renderClocktowerGrimoire()}
      <button class="danger" data-action="reset-clocktower">${tr("restart")}</button>
    </div>
  `;
}

function renderClocktowerSummary() {
  const c = state.clocktower;
  const alive = ctAlive();
  const threshold = ctExecutionThreshold();
  const highestVoteIds = ctHighestVoteIds();
  const highestVoteText = highestVoteIds.length > 1
    ? `${state.language === "en" ? "Tie" : "平票"}：${highestVoteIds.map(ctSeatLabelById).join(" / ")}，${c.highestVoteCount}`
    : highestVoteIds.length === 1 ? `${ctSeatLabelById(highestVoteIds[0])}，${c.highestVoteCount}` : tr("noDeath");
  const phaseText = c.phase === "firstNight" ? tr("firstNight") : c.phase === "night" ? `${tr("night")} ${c.round}` : c.phase === "dawn" ? tr("dawn") : `${tr("day")} ${c.round}`;
  return `
    <div class="host-progress">
      <strong>${phaseText}</strong>
      <span>${state.language === "en" ? "Alive" : "存活玩家"}：${alive.length} · ${state.language === "en" ? "Votes to execute" : "处决所需票数"}：${threshold}</span>
      <span>${state.language === "en" ? "Highest vote" : "当前最高票"}：${highestVoteText}</span>
    </div>
  `;
}

function ctHighestVoteIds() {
  const c = state.clocktower;
  if (Array.isArray(c.highestVoteNames) && c.highestVoteNames.length) {
    return Array.from(new Set(c.highestVoteNames.filter(Boolean)));
  }
  return c.highestVoteName ? [c.highestVoteName] : [];
}

function renderClocktowerTestLab() {
  const c = state.clocktower;
  const currentSteps = c.phase === "firstNight" ? ctFirstNightSteps() : ctLaterNightSteps();
  const currentStep = c.nightStarted && ["firstNight", "night"].includes(c.phase)
    ? currentSteps[Math.min(c.nightIndex, currentSteps.length - 1)]
    : null;
  if (!c.testLabOpen) {
    return `<button class="test-lab-toggle" data-action="ct-test-toggle">🧪 ${state.language === "en" ? "Open test lab controls" : "展开测试实验室控制台"}</button>`;
  }
  const scenarios = [
    ["poison-empath", state.language === "en" ? "Poisoned Empath" : "中毒共情者"],
    ["poison-demon", state.language === "en" ? "Poisoned Demon" : "中毒恶魔"],
    ["poison-slayer", state.language === "en" ? "Poisoned Slayer" : "中毒猎手"],
    ["poison-virgin", state.language === "en" ? "Poisoned Virgin" : "中毒处女"],
    ["poison-ravenkeeper", state.language === "en" ? "Poisoned Ravenkeeper" : "中毒守鸦人"],
    ["vote-tie", state.language === "en" ? "Tied highest vote" : "最高票平票"],
  ];
  return `
    <section class="test-lab-panel">
      <div class="toolbar">
        <div>
          <div class="step-kicker">${state.language === "en" ? "TEST MODE · local data only" : "测试模式 · 仅修改本机数据"}</div>
          <h2>${state.language === "en" ? "Character test lab" : "角色测试实验室"}</h2>
        </div>
        <button class="quiet" data-action="ct-test-toggle">${state.language === "en" ? "Collapse" : "收起"}</button>
      </div>

      <div class="test-lab-block">
        <strong>${state.language === "en" ? "Quick scenarios" : "快速场景预设"}</strong>
        <div class="test-button-grid">
          ${scenarios.map(([id, label]) => `<button class="ghost" data-action="ct-test-scenario" data-scenario="${id}">${label}</button>`).join("")}
        </div>
      </div>

      <div class="test-lab-block">
        <strong>${state.language === "en" ? "Load any character" : "载入任意角色"}</strong>
        <div class="test-role-loader">
          <select id="ctTestRoleSelect" aria-label="${state.language === "en" ? "Character" : "角色"}">
            ${TB_ROLES.map((role) => `<option value="${role.id}">${htmlEscape(nameOfRole(role.id))} · ${teamLabel(role.team)}</option>`).join("")}
          </select>
          <button class="primary" data-action="ct-test-load-role">${state.language === "en" ? "Load and jump" : "载入并跳转"}</button>
        </div>
      </div>

      <div class="test-lab-block">
        <strong>${state.language === "en" ? "Jump to phase" : "跳转阶段"}</strong>
        <div class="test-button-grid three">
          <button class="ghost" data-action="ct-test-phase" data-phase="firstNight">${tr("firstNight")}</button>
          <button class="ghost" data-action="ct-test-phase" data-phase="night">${state.language === "en" ? "Later night" : "后续夜晚"}</button>
          <button class="ghost" data-action="ct-test-phase" data-phase="day">${tr("day")}</button>
        </div>
      </div>

      ${["firstNight", "night"].includes(c.phase) ? `
        <div class="test-lab-block">
          <strong>${state.language === "en" ? "Jump to night step" : "跳转夜晚步骤"}</strong>
          <div class="test-step-list">
            ${currentSteps.map((step, index) => `<button class="chip ${currentStep === step ? "selected" : ""}" data-action="ct-test-step" data-index="${index}">${index + 1}. ${htmlEscape(step.title)}</button>`).join("")}
          </div>
        </div>
      ` : ""}

      <div class="test-status-strip">
        <span>${state.language === "en" ? "Phase" : "阶段"}：${c.phase}</span>
        <span>${state.language === "en" ? "Current" : "当前"}：${htmlEscape(currentStep?.title || c.dayMode)}</span>
        <span>${state.language === "en" ? "Poisoned" : "中毒"}：${c.poisonTarget ? htmlEscape(ctSeatLabelById(c.poisonTarget)) : tr("noDeath")}</span>
      </div>
      ${c.testNotice ? `<div class="instruction warn">${htmlEscape(c.testNotice)}</div>` : ""}

      <div class="test-lab-block">
        <strong>${state.language === "en" ? "Edit roster" : "编辑角色与状态"}</strong>
        <div class="test-roster">
          ${ctPlayers().map((player) => `
            <div class="test-player-row ${player.alive ? "" : "dead"}">
              <span class="compact-seat">${player.seat}</span>
              <span>${htmlEscape(player.name)}</span>
              <select data-change="ct-test-role" data-id="${player.id}" aria-label="${htmlEscape(player.name)} ${state.language === "en" ? "role" : "角色"}">
                ${TB_ROLES.map((role) => `<option value="${role.id}" ${player.actualRole === role.id ? "selected" : ""}>${htmlEscape(nameOfRole(role.id))}</option>`).join("")}
              </select>
              <button class="mini" data-action="ct-test-alive" data-id="${player.id}">${player.alive ? (state.language === "en" ? "Alive" : "存活") : (state.language === "en" ? "Dead" : "死亡")}</button>
              <button class="mini ${c.poisonTarget === player.id ? "poisoned" : ""}" data-action="ct-test-poison" data-id="${player.id}">${c.poisonTarget === player.id ? (state.language === "en" ? "Poisoned" : "已中毒") : (state.language === "en" ? "Poison" : "设为中毒")}</button>
            </div>
          `).join("")}
        </div>
      </div>

      <button class="danger" data-action="ct-test-exit">${state.language === "en" ? "Exit test game" : "退出并清空测试局"}</button>
    </section>
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
  const displayButtons = step.displayOptions && step.displayOptions.length
    ? step.displayOptions.map((option, index) => `<button class="ghost display-choice" data-action="show-display" data-card="${htmlEscape(encodeURIComponent(JSON.stringify(option.display)))}">${htmlEscape(option.label || `${state.language === "en" ? "Show option" : "展示选项"} ${index + 1}`)}</button>`).join("")
    : (step.display && step.control !== "fortune" ? `<button class="ghost" data-action="show-display" data-card="${htmlEscape(encodeURIComponent(JSON.stringify(step.display)))}">${tr("showToPlayer")}</button>` : "");
  return `
    <div class="step-card host-step">
      <div class="step-kicker">${htmlEscape(step.title)} · ${step.real ? tr("realAction") : (state.language === "en" ? "Placeholder" : "占位")}</div>
      <h2 class="host-command">${htmlEscape(step.headline || ctStepHeadline(step))}</h2>
      <div class="step-actions">
        ${renderClocktowerActionControl(step)}
        ${step.tell ? `<div class="big-answer">${nl(step.tell)}</div>` : ""}
        ${step.displayOptions && step.displayOptions.length ? `<div class="option-hint">${state.language === "en" ? "Unreliable ability: choose one result to show." : "能力不可靠：请选择一个结果展示。"}</div>` : ""}
        <div class="button-row">
          ${displayButtons}
          <button class="primary" data-action="ct-next-night">${tr("doneNext")}</button>
        </div>
      </div>
      ${note ? `<p class="step-note ${step.real ? "" : "warn"}">${nl(note)}</p>` : ""}
    </div>
  `;
}

function clocktowerScriptName(id) {
  const script = CLOCKTOWER_SCRIPTS.find((item) => item.id === id);
  return script ? (state.language === "en" ? script.en : script.zh) : id;
}

function defaultClocktowerScriptFor(count) {
  return count === 5 || count === 6 ? "noGreaterJoy" : "troubleBrewing";
}

function effectiveClocktowerScript() {
  const count = state.seats.length;
  return count === 5 || count === 6 ? (state.selectedClocktowerScript || "noGreaterJoy") : "troubleBrewing";
}

function canStartClocktowerScript(scriptId) {
  return CLOCKTOWER_SCRIPTS.some((script) => script.id === scriptId && script.enabled);
}

function clocktowerRolesForScript(scriptId) {
  const ids = scriptId === "noGreaterJoy" ? NO_GREATER_JOY_ROLE_IDS : TROUBLE_BREWING_ROLE_IDS;
  return ids.map(getRole).filter(Boolean);
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
  if (step.control === "chambermaid") {
    const actor = ctPlayers().find((p) => p.id === step.actorId);
    const candidates = alive.filter((p) => p.id !== actor?.id);
    const ready = state.clocktower.chambermaidFirst && state.clocktower.chambermaidSecond;
    return `
      <div class="fortune-control">
        <div class="number-picker">
          ${renderNumberPicker("ct-select", "chambermaidFirst", candidates, state.clocktower.chambermaidFirst, step.real)}
        </div>
        <div class="number-picker">
          ${renderNumberPicker("ct-select", "chambermaidSecond", candidates.filter((p) => p.id !== state.clocktower.chambermaidFirst), state.clocktower.chambermaidSecond, step.real)}
        </div>
        <button class="primary query-button" data-action="ct-show-chambermaid" ${ready && step.real ? "" : "disabled"}>
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
  if (c.dayMode === "slayer") return renderSlayerAbility();
  if (c.dayMode === "artist") return renderArtistQuestion();
  if (c.dayMode === "klutz") return renderKlutzChoice();
  if (c.dayMode === "vote") return renderVote();
  if (c.dayMode === "endConfirm") return renderEndDay();
  return renderDayOverview();
}

function renderDayOverview() {
  const c = state.clocktower;
  const scriptHasSlayer = clocktowerRolesForScript(c.scriptId || "troubleBrewing").some((role) => role.id === "slayer");
  const scriptHasArtist = clocktowerRolesForScript(c.scriptId || "troubleBrewing").some((role) => role.id === "artist");
  const slayerClaimants = ctAlive().filter((p) => !(c.slayerClaimantsUsed || []).includes(p.id) && !(c.slayerUsed && p.actualRole === "slayer"));
  const artistClaimants = ctAlive().filter((p) => !(c.artistClaimantsUsed || []).includes(p.id) && !(c.artistUsed && p.actualRole === "artist"));
  return `
    <div class="step-card">
      <h2>${state.language === "en" ? `Day ${c.round}` : `第 ${c.round} 天 白天`}</h2>
      <div class="instruction">
        ${state.language === "en"
          ? "Manage nominations, votes, execution, dead votes, one-use abilities, and win checks."
          : "管理提名、投票、处决、亡者票、一次性能力和胜负检查。"}
      </div>
      <button class="primary" data-action="ct-day-mode" data-mode="nomination">${state.language === "en" ? "Start nomination" : "开始提名"}</button>
      ${scriptHasSlayer ? `<button class="danger" data-action="ct-day-mode" data-mode="slayer" ${slayerClaimants.length ? "" : "disabled"}>${state.language === "en" ? "Slayer action" : "杀手行动"}</button>` : ""}
      ${scriptHasArtist ? `<button class="ghost" data-action="ct-day-mode" data-mode="artist" ${artistClaimants.length ? "" : "disabled"}>${state.language === "en" ? "Artist question" : "艺术家提问"}</button>` : ""}
      <button class="ghost" data-action="ct-day-mode" data-mode="endConfirm">${state.language === "en" ? "End day" : "结束白天"}</button>
    </div>
  `;
}

function renderSlayerAbility() {
  const c = state.clocktower;
  const claimants = ctAlive().filter((p) => !(c.slayerClaimantsUsed || []).includes(p.id) && !(c.slayerUsed && p.actualRole === "slayer"));
  const targets = ctAlive().filter((p) => p.id !== c.slayerClaimant);
  return `
    <div class="step-card">
      <h2>${state.language === "en" ? "Slayer action" : "杀手行动"}</h2>
      <div class="instruction">
        ${state.language === "en"
          ? "Choose the player publicly claiming Slayer, then choose the target. Used claimants will not appear again."
          : "选择公开声称自己是杀手的玩家，再选择目标。已经声称过的人不会再出现。"}
      </div>
      <div class="instruction">
        <strong>${state.language === "en" ? "Claimant" : "声称者"}</strong>
        ${claimants.length ? renderChipPicker("ct-select", "slayerClaimant", claimants, c.slayerClaimant) : `<div class="muted">${state.language === "en" ? "No available claimants." : "没有可选声称者。"}</div>`}
      </div>
      <div class="instruction">
        <strong>${state.language === "en" ? "Target" : "目标"}</strong>
        ${renderChipPicker("ct-select", "slayerTarget", targets, c.slayerTarget)}
      </div>
      <button class="danger" data-action="ct-fire-slayer" ${c.slayerClaimant && c.slayerTarget ? "" : "disabled"}>${state.language === "en" ? "Resolve Slayer action" : "结算杀手行动"}</button>
      <button class="ghost" data-action="ct-day-mode" data-mode="overview">${state.language === "en" ? "Back to day" : "返回白天"}</button>
    </div>
  `;
}

function renderArtistQuestion() {
  const c = state.clocktower;
  const claimants = ctAlive().filter((p) => !(c.artistClaimantsUsed || []).includes(p.id) && !(c.artistUsed && p.actualRole === "artist"));
  return `
    <div class="step-card">
      <h2>${state.language === "en" ? "Artist question" : "艺术家提问"}</h2>
      <div class="instruction">
        ${state.language === "en"
          ? "Choose the player claiming Artist. If this is the real Artist's first question, answer one yes/no question privately."
          : "选择声称自己是艺术家的玩家。如果是真艺术家首次提问，请私下回答一个是/否问题。"}
      </div>
      <div class="instruction">
        <strong>${state.language === "en" ? "Claimant" : "提问者"}</strong>
        ${claimants.length ? renderChipPicker("ct-select", "artistClaimant", claimants, c.artistClaimant) : `<div class="muted">${state.language === "en" ? "No available claimants." : "没有可选提问者。"}</div>`}
      </div>
      <button class="primary" data-action="ct-confirm-artist" ${c.artistClaimant ? "" : "disabled"}>${state.language === "en" ? "Record Artist question" : "记录艺术家提问"}</button>
      <button class="ghost" data-action="ct-day-mode" data-mode="overview">${state.language === "en" ? "Back to day" : "返回白天"}</button>
    </div>
  `;
}

function renderKlutzChoice() {
  const c = state.clocktower;
  const klutz = c.pendingKlutz ? ctPlayers().find((p) => p.id === c.pendingKlutz) : null;
  return `
    <div class="step-card">
      <h2>${state.language === "en" ? "Klutz choice" : "呆瓜选择"}</h2>
      <div class="instruction">
        ${klutz ? ctSeatLabel(klutz) : ""} ${state.language === "en"
          ? "is the Klutz. After learning they died, they must publicly choose a living player."
          : "是呆瓜。得知自己死亡后，必须公开选择一名存活玩家。"}
      </div>
      <div class="instruction">
        <strong>${state.language === "en" ? "Chosen player" : "选择玩家"}</strong>
        ${renderChipPicker("ct-select", "klutzChoice", ctAlive(), c.klutzChoice)}
      </div>
      <button class="danger" data-action="ct-confirm-klutz" ${c.klutzChoice ? "" : "disabled"}>${state.language === "en" ? "Confirm Klutz choice" : "确认呆瓜选择"}</button>
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
  const threshold = ctExecutionThreshold();
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
  const threshold = ctExecutionThreshold();
  const highestVoteIds = ctHighestVoteIds();
  const tied = c.highestVoteCount >= threshold && highestVoteIds.length > 1;
  const target = c.highestVoteCount >= threshold && highestVoteIds.length === 1 ? highestVoteIds[0] : null;
  return `
    <div class="step-card">
      <h2>${state.language === "en" ? "Prepare to end day" : "准备结束白天"}</h2>
      <div class="instruction">
        ${tied
          ? (state.language === "en" ? `Tie on ${c.highestVoteCount} votes: ${highestVoteIds.map(ctSeatLabelById).join(" / ")}. No player will be executed.` : `最高票 ${c.highestVoteCount} 票平票：${highestVoteIds.map(ctSeatLabelById).join(" / ")}。今天无人被处决。`)
          : target
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

function ctEventPhaseLabel(event) {
  if (event.phase === "setup") return state.language === "en" ? "Setup" : "开局";
  if (event.phase === "legacy") return state.language === "en" ? "Imported" : "旧记录";
  if (event.phase === "firstNight") return tr("firstNight");
  if (event.phase === "night") return state.language === "en" ? `Night ${event.round}` : `第 ${event.round} 夜`;
  if (event.phase === "day") return state.language === "en" ? `Day ${event.round}` : `第 ${event.round} 天`;
  if (event.phase === "dawn") return tr("dawn");
  return state.language === "en" ? "Game" : "游戏";
}

function ctEventTypeClass(type) {
  if (["death", "execution", "gameEnd"].includes(type)) return "danger";
  if (["roleAction", "infoShown", "roleChange"].includes(type)) return "action";
  if (["nomination", "vote"].includes(type)) return "vote";
  if (type === "phase") return "phase";
  return "system";
}

function ctEventIcon(type) {
  const map = {
    system: "•",
    phase: "↴",
    roleAction: "◆",
    infoShown: "◈",
    death: "×",
    execution: "×",
    roleChange: "⇄",
    nomination: "◇",
    vote: "#",
    gameEnd: "!",
    legacy: "•",
  };
  return map[type] || "•";
}

function renderClocktowerTimeline() {
  const events = [...(state.clocktower.events || [])]
    .filter((event) => !["system", "phase"].includes(event.type))
    .filter((event) => event.type !== "infoShown" || event.meta?.unreliable === true)
    .sort((a, b) => Number(a.sequence || 0) - Number(b.sequence || 0));
  if (!events.length) {
    return `<div class="empty">${state.language === "en" ? "No game records yet." : "还没有游戏记录。"}</div>`;
  }
  const groups = [];
  events.forEach((event) => {
    const key = `${event.phase}:${event.round}`;
    let group = groups.find((item) => item.key === key);
    if (!group) {
      group = { key, label: ctEventPhaseLabel(event), events: [] };
      groups.push(group);
    }
    group.events.push(event);
  });
  return `
    <div class="timeline-list">
      ${groups.map((group) => `
        <section class="timeline-group">
          <div class="timeline-group-title">${htmlEscape(group.label)}</div>
          ${group.events.map((event) => `
            <div class="timeline-row ${ctEventTypeClass(event.type)}">
              <div class="timeline-icon" aria-hidden="true">${ctEventIcon(event.type)}</div>
              <div class="timeline-body">
                <strong>${htmlEscape(event.title)}</strong>
                ${event.detail ? `<div class="timeline-detail">${htmlEscape(event.detail)}</div>` : ""}
              </div>
            </div>
          `).join("")}
        </section>
      `).join("")}
    </div>
  `;
}

function ctVisibleEventCount() {
  return (state.clocktower.events || [])
    .filter((event) => !["system", "phase"].includes(event.type))
    .filter((event) => event.type !== "infoShown" || event.meta?.unreliable === true)
    .length;
}

function renderClocktowerGrimoire() {
  const c = state.clocktower;
  const open = c.grimoireOpen !== false;
  const eventCount = ctVisibleEventCount();
  return `
    <div class="card grimoire-panel">
      <button class="grimoire-toggle" data-action="ct-toggle-grimoire" aria-expanded="${open ? "true" : "false"}">
        <span>${state.language === "en" ? "Game record" : "游戏记录"}</span>
        <span class="grimoire-count">${state.language === "en" ? `${eventCount} records` : `${eventCount} 条记录`}</span>
        <span class="grimoire-caret" aria-hidden="true">${open ? "⌃" : "⌄"}</span>
      </button>
      ${open ? `
        <div class="timeline-section">
          ${renderClocktowerTimeline()}
        </div>
        <div class="timeline-heading roster-heading">
          <strong>${state.language === "en" ? "Players" : "角色信息"}</strong>
        </div>
        <div class="compact-roster">
          ${c.players.map((p) => {
            const role = `${nameOfRole(p.actualRole)}${p.shownRole !== p.actualRole ? ` / ${state.language === "en" ? "shown" : "显示"} ${nameOfRole(p.shownRole)}` : ""}`;
            const evil = ["minion", "demon"].includes(ctTeam(p));
            return `
              <div class="compact-player ${p.alive ? "alive" : "dead"}">
                <div class="compact-seat">${p.seat}</div>
                <span class="life-dot ${p.alive ? "alive" : "dead"}" title="${p.alive ? (state.language === "en" ? "Alive" : "存活") : (state.language === "en" ? "Dead" : "死亡")}"></span>
                <div class="compact-main">
                  <strong>${htmlEscape(p.name)}</strong>
                  <span>${role}</span>
                </div>
                <span class="role-tag compact ${evil ? "evil" : ""}">${teamLabel(ctTeam(p))}</span>
              </div>
            `;
          }).join("")}
        </div>
      ` : ""}
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
  const scriptId = effectiveClocktowerScript();
  if (!canStartClocktowerScript(scriptId)) return;
  const scriptRoles = clocktowerRolesForScript(scriptId);
  const dist = ctDistribution(count);
  const demon = getRole("imp");
  const minions = takeRandom(scriptRoles.filter((r) => r.team === "minion"), dist.minion);
  const hasBaron = minions.some((r) => r.id === "baron");
  const baronOutsiderIncrease = hasBaron ? (scriptId === "noGreaterJoy" ? Math.max(0, Math.min(2, 2 - dist.outsider)) : 2) : 0;
  const outsiderCount = dist.outsider + baronOutsiderIncrease;
  const townsfolkCount = Math.max(0, dist.townsfolk - baronOutsiderIncrease);
  const outsiders = takeRandom(scriptRoles.filter((r) => r.team === "outsider"), outsiderCount);
  const townsfolk = takeRandom(scriptRoles.filter((r) => r.team === "townsfolk"), townsfolkCount);
  const actualRoles = shuffle([demon, ...minions, ...outsiders, ...townsfolk]);
  const actualIds = new Set(actualRoles.map((r) => r.id));
  const outOfPlayGood = scriptRoles.filter((r) => ["townsfolk", "outsider"].includes(r.team) && !actualIds.has(r.id));
  const bluffs = takeRandom(outOfPlayGood, 3).map((r) => r.id);
  const fakeDrunkRoles = scriptRoles.filter((r) => r.team === "townsfolk" && !actualIds.has(r.id));
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
    scriptId,
    players,
    bluffs,
  };
}

function ctStartTestGame(roleId = "empath") {
  const scriptId = TROUBLE_BREWING_ROLE_IDS.includes(roleId) ? "troubleBrewing" : "noGreaterJoy";
  const scriptRoles = clocktowerRolesForScript(scriptId);
  const required = [roleId, "imp", "poisoner", "empath", "slayer", "virgin", "ravenkeeper", "washerwoman"];
  const roleIds = [];
  [...required, ...scriptRoles.map((role) => role.id)].forEach((id) => {
    if (getRole(id) && (id === roleId || scriptRoles.some((role) => role.id === id)) && !roleIds.includes(id)) roleIds.push(id);
  });
  while (roleIds.length < 8) {
    const fallback = TB_ROLES.find((role) => !roleIds.includes(role.id));
    if (!fallback) break;
    roleIds.push(fallback.id);
  }
  const seats = Array.from({ length: 8 }, (_, index) => ({
    id: `ct-test-${index + 1}`,
    name: state.language === "en" ? `Tester ${index + 1}` : `测试员${index + 1}`,
    type: "temp",
  }));
  state.seats = seats;
  state.selectedGame = "clocktower";
  state.clocktower = {
    ...freshClocktower(),
    started: true,
    testMode: true,
    testLabOpen: true,
    scriptId,
    players: seats.map((seat, index) => {
      const actualRole = roleIds[index];
      return {
        id: seat.id,
        name: seat.name,
        seat: index + 1,
        actualRole,
        shownRole: actualRole === "drunk" ? "empath" : actualRole,
        alive: true,
      };
    }),
    bluffs: ["chef", "butler", "mayor"],
  };
  ctConfigureRoleTest(roleId);
}

function ctConfigureRoleTest(roleId) {
  const c = state.clocktower;
  const actor = ctPlayers().find((p) => p.actualRole === roleId) || ctPlayers()[0];
  const effectiveRoleId = roleId === "drunk" ? actor.shownRole : roleId;
  c.outcome = null;
  c.testNotice = "";
  c.phase = "firstNight";
  c.round = 1;
  c.nightStarted = true;
  c.nightIndex = 0;
  c.dayMode = "overview";
  c.pendingNightDeath = null;
  c.lastExecuted = null;
  c.nominator = null;
  c.nominee = null;
  c.slayerClaimant = null;
  c.artistClaimant = null;

  const dayModes = { slayer: "slayer", virgin: "nomination", artist: "artist", klutz: "klutz" };
  if (dayModes[effectiveRoleId]) {
    c.phase = "day";
    c.nightStarted = false;
    c.dayMode = dayModes[effectiveRoleId];
    if (effectiveRoleId === "slayer") c.slayerClaimant = actor.id;
    if (effectiveRoleId === "virgin") c.nominee = actor.id;
    if (effectiveRoleId === "artist") c.artistClaimant = actor.id;
    if (effectiveRoleId === "klutz") {
      actor.alive = false;
      c.pendingKlutz = actor.id;
    }
    return;
  }

  const laterPreferred = new Set(["undertaker", "empath", "chambermaid", "fortuneTeller", "butler", "poisoner", "imp", "sage", "ravenkeeper"]);
  if (laterPreferred.has(effectiveRoleId)) {
    c.phase = effectiveRoleId === "poisoner" ? "firstNight" : "night";
    c.round = c.phase === "night" ? 2 : 1;
  }
  if (["sage", "ravenkeeper"].includes(effectiveRoleId)) {
    c.pendingNightDeath = actor.id;
    c.ravenkeeperTarget = ctPlayers().find((p) => p.id !== actor.id)?.id || null;
  }
  if (effectiveRoleId === "undertaker") {
    const executed = ctPlayers().find((p) => p.id !== actor.id);
    c.lastExecuted = executed?.id || null;
  }
  const steps = c.phase === "firstNight" ? ctFirstNightSteps() : ctLaterNightSteps();
  const index = steps.findIndex((step) => step.roleId === effectiveRoleId);
  if (index >= 0) {
    c.nightIndex = index;
  } else {
    c.phase = "day";
    c.nightStarted = false;
    c.dayMode = "overview";
    c.testNotice = state.language === "en"
      ? `${nameOfRole(roleId)} currently has no independent guided step. The role is loaded; use the roster and phase controls to test its passive or not-yet-connected conditions.`
      : `${nameOfRole(roleId)} 当前没有独立引导步骤。角色已经载入，可使用角色表和阶段控制测试其被动条件或尚未接入的逻辑。`;
  }
}

function ctLoadTestScenario(scenarioId) {
  const roleByScenario = {
    "poison-empath": "empath",
    "poison-demon": "imp",
    "poison-slayer": "slayer",
    "poison-virgin": "virgin",
    "poison-ravenkeeper": "ravenkeeper",
  };
  const roleId = roleByScenario[scenarioId] || "empath";
  ctStartTestGame(roleId);
  const c = state.clocktower;
  if (scenarioId === "vote-tie") {
    const threshold = ctExecutionThreshold();
    c.phase = "day";
    c.nightStarted = false;
    c.dayMode = "endConfirm";
    c.highestVoteCount = threshold;
    c.highestVoteNames = ctPlayers().slice(0, 2).map((player) => player.id);
    c.highestVoteName = null;
    c.testNotice = state.language === "en"
      ? "Two players are tied at the execution threshold. Confirm that the day ends with no execution."
      : "两名玩家在处决门槛上平票。请确认结束白天时无人被处决。";
    return;
  }
  const actor = ctPlayers().find((p) => p.actualRole === roleId);
  if (!actor) return;
  c.poisonTarget = actor.id;
  if (roleId === "virgin") {
    const nominator = ctPlayers().find((p) => p.id !== actor.id && teamOfRole(p.actualRole) === "townsfolk");
    c.nominator = nominator?.id || null;
    c.nominee = actor.id;
  }
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

function ctExecutionThreshold() {
  return Math.ceil(ctAlive().length / 2);
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

function ctCompactText(value) {
  return String(value || "").replace(/\s+/g, " ").trim();
}

function ctOutcome(winner, reason) {
  const c = state.clocktower;
  if (c.outcome) return;
  c.outcome = { winner, reason };
  ctAddEvent({
    type: "gameEnd",
    title: winner,
    detail: reason,
    phase: c.phase,
    round: c.round,
  });
}

function ctRecordNightStep(step) {
  if (!step || !step.real) return;
  const c = state.clocktower;
  const actor = step.actorId ? ctPlayers().find((p) => p.id === step.actorId) : null;
  const actorLabel = actor ? ctSeatLabel(actor) : step.title;
  const eventBase = {
    type: "roleAction",
    title: step.title,
    phase: c.phase,
    round: c.round,
    playerIds: actor ? [actor.id] : [],
  };
  if (step.control === "redHerring" && c.redHerring) {
    ctAddEvent({
      ...eventBase,
      title: state.language === "en" ? "Red herring chosen" : "红鲱鱼选择",
      detail: ctSeatLabelById(c.redHerring),
      playerIds: [c.redHerring],
    });
    return;
  }
  if (step.control === "poisonTarget" && c.poisonTarget) {
    ctAddEvent({
      ...eventBase,
      detail: `${actorLabel} → ${ctSeatLabelById(c.poisonTarget)}`,
      playerIds: [...eventBase.playerIds, c.poisonTarget],
    });
    return;
  }
  if (step.control === "butlerMaster" && c.butlerMaster) {
    ctAddEvent({
      ...eventBase,
      detail: `${actorLabel} → ${ctSeatLabelById(c.butlerMaster)}`,
      playerIds: [...eventBase.playerIds, c.butlerMaster],
    });
    return;
  }
  if (step.control === "nightDeath" && c.pendingNightDeath) {
    const blocked = ctDemonPoisonedTonight();
    ctAddEvent({
      ...eventBase,
      title: state.language === "en" ? "Demon choice" : "恶魔选择",
      detail: `${ctSeatLabelById(c.pendingNightDeath)}${blocked ? (state.language === "en" ? " · poisoned" : " · 中毒失效") : ""}`,
      playerIds: [...eventBase.playerIds, c.pendingNightDeath],
    });
    return;
  }
  if (step.control === "ravenkeeperTarget" && c.ravenkeeperTarget) {
    const target = ctPlayers().find((p) => p.id === c.ravenkeeperTarget);
    ctAddEvent({
      ...eventBase,
      detail: target ? `${actorLabel} → ${ctSeatLabel(target)} · ${nameOfRole(target.actualRole)}` : "",
      playerIds: [...eventBase.playerIds, c.ravenkeeperTarget],
    });
    return;
  }
  if (step.control === "fortune" && c.fortuneFirst && c.fortuneSecond) {
    const result = ctFortuneResult() ? tr("yes") : tr("no");
    ctAddEvent({
      ...eventBase,
      detail: `${ctSeatLabelById(c.fortuneFirst)} / ${ctSeatLabelById(c.fortuneSecond)} · ${result}`,
      playerIds: [...eventBase.playerIds, c.fortuneFirst, c.fortuneSecond],
    });
    return;
  }
  if (step.control === "chambermaid" && c.chambermaidFirst && c.chambermaidSecond) {
    ctAddEvent({
      ...eventBase,
      detail: `${ctSeatLabelById(c.chambermaidFirst)} / ${ctSeatLabelById(c.chambermaidSecond)} · ${ctChambermaidResult()}`,
      playerIds: [...eventBase.playerIds, c.chambermaidFirst, c.chambermaidSecond],
    });
    return;
  }
}

function ctRecordDisplayShown(display) {
  const c = state.clocktower;
  if (!c.started || !c.nightStarted) return;
  const step = ctNightSteps()[Math.min(c.nightIndex, ctNightSteps().length - 1)];
  if (!step || !step.real) return;
  const actor = step.actorId ? ctPlayers().find((p) => p.id === step.actorId) : null;
  const unreliable = actor && (actor.actualRole === "drunk" || c.poisonTarget === actor.id);
  if (!unreliable) return;
  const parts = [display.title, display.primary, display.subhead, display.secondary, display.footer].filter(Boolean);
  if (!parts.length) return;
  ctAddEvent({
    type: "infoShown",
    title: state.language === "en" ? "Unreliable information" : "不可靠信息",
    detail: state.language === "en"
      ? `${actor ? ctSeatLabel(actor) : step.title}: ${ctCompactText(parts.join(" "))}`
      : `${actor ? ctSeatLabel(actor) : step.title}：${ctCompactText(parts.join(" "))}`,
    playerIds: actor ? [actor.id] : [],
    phase: c.phase,
    round: c.round,
    meta: { stepTitle: step.title, unreliable: true },
  });
}

function ctRoleActor(roleId) {
  return ctAlive().find((p) => p.actualRole === roleId || (p.actualRole === "drunk" && p.shownRole === roleId));
}

function ctAnyRole(roleId) {
  return ctPlayers().find((p) => p.actualRole === roleId);
}

function ctMissingReason(roleId) {
  const role = ctAnyRole(roleId);
  const drunkShownAsRole = ctPlayers().find((p) => p.actualRole === "drunk" && p.shownRole === roleId);
  if (!role && drunkShownAsRole) return "";
  if (!role) return state.language === "en" ? "This character is not in play." : "本局没有这个角色。";
  if (!role.alive) return state.language === "en" ? `${ctSeatLabel(role)} is dead and no longer acts.` : `${ctSeatLabel(role)} 已经死亡，死亡后不再执行这个能力。`;
  return "";
}

function ctNightSteps() {
  return state.clocktower.phase === "firstNight" ? ctFirstNightSteps() : ctLaterNightSteps();
}

function ctStep({ title, actor, real, reason, wakeText, actionText, tell, explain, control, display, displayOptions, headline, tip, roleId }) {
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
    displayOptions: displayOptions || [],
    headline,
    tip,
    roleId,
  };
}

function ctFilterNightSteps(steps) {
  const scriptId = state.clocktower.scriptId || "troubleBrewing";
  const roleIds = new Set(clocktowerRolesForScript(scriptId).map((role) => role.id));
  return steps.filter((step) => {
    const roleAllowed = !step.roleId || roleIds.has(step.roleId);
    const smallGameEvilInfo = scriptId === "noGreaterJoy" && ["Minion info", "Demon info", "爪牙信息", "恶魔信息"].includes(step.title);
    return roleAllowed && !smallGameEvilInfo;
  });
}

function ctFirstNightSteps() {
  const c = state.clocktower;
  const demon = ctPlayers().find((p) => ctTeam(p) === "demon");
  const minions = ctPlayers().filter((p) => ctTeam(p) === "minion");
  const minionNames = minions.map(ctSeatLabel).join("、") || (state.language === "en" ? "none" : "无");
  const bluffNames = c.bluffs.map(nameOfRole).join(" / ");
  const shouldGiveFirstNightEvilInfo = ctPlayers().length >= 7;
  return ctFilterNightSteps([
    ctStep({
      title: state.language === "en" ? "Minion info" : "爪牙信息",
      actor: shouldGiveFirstNightEvilInfo ? minions[0] : null,
      real: minions.length > 0 && shouldGiveFirstNightEvilInfo,
      reason: !shouldGiveFirstNightEvilInfo ? (state.language === "en" ? "In 5-6 player games, evil players do not receive first-night info." : "5–6 人局首夜不给邪恶方互认和伪装信息。") : (minions.length ? "" : (state.language === "en" ? "No Minions are in play." : "本局没有爪牙。")),
      wakeText: minions.length && shouldGiveFirstNightEvilInfo ? `${state.language === "en" ? "Wake all Minions" : "请唤醒所有爪牙"}：${minionNames}。` : "",
      headline: minions.length && shouldGiveFirstNightEvilInfo ? (state.language === "en" ? "Wake all Minions" : "唤醒所有爪牙") : (state.language === "en" ? "Minion placeholder" : "爪牙信息的占位操作"),
      actionText: state.language === "en"
        ? `Let the Minions recognize each other.\nTell them the Demon is ${demon ? ctSeatLabel(demon) : "unknown"}.\nSignal them to close their eyes.`
        : `示意爪牙互相确认。\n告诉他们恶魔是 ${demon ? ctSeatLabel(demon) : "未知"}。\n确认后示意他们闭眼。`,
      tell: demon && shouldGiveFirstNightEvilInfo ? `${state.language === "en" ? "The Demon is" : "恶魔是"}：\n${ctSeatLabel(demon)}` : null,
      explain: shouldGiveFirstNightEvilInfo ? (state.language === "en" ? "On the first night, Minions learn who the Demon is." : "首夜爪牙需要知道恶魔是谁，并确认彼此身份。") : (state.language === "en" ? "Small games skip first-night evil info." : "小局跳过首夜邪恶方信息。"),
      tip: state.language === "en" ? "Show the Demon seat, then close their eyes." : "展示恶魔座位，确认后让他们闭眼。",
      display: demon && shouldGiveFirstNightEvilInfo ? { title: state.language === "en" ? "Minion info" : "爪牙信息", primary: state.language === "en" ? "Demon" : "恶魔", secondary: ctSeatLabel(demon), footer: state.language === "en" ? "This player is the Demon." : "这名玩家是恶魔。" } : null,
    }),
    ctStep({
      title: state.language === "en" ? "Demon info" : "恶魔信息",
      actor: shouldGiveFirstNightEvilInfo ? demon : null,
      real: !!demon && shouldGiveFirstNightEvilInfo,
      reason: !shouldGiveFirstNightEvilInfo ? (state.language === "en" ? "In 5-6 player games, the Demon does not receive bluffs." : "5–6 人局恶魔不获得伪装身份。") : (demon ? "" : (state.language === "en" ? "There is no Demon right now." : "当前没有恶魔。")),
      headline: demon && shouldGiveFirstNightEvilInfo ? (state.language === "en" ? `Wake ${ctSeatLabel(demon)}` : `唤醒 ${ctSeatLabel(demon)}`) : (state.language === "en" ? "Demon placeholder" : "恶魔信息的占位操作"),
      actionText: state.language === "en"
        ? `Tell the Demon who the Minions are.\nTell the Demon the three bluff characters.\nThe Demon does not kill on night 1.`
        : `告诉恶魔爪牙是谁。\n告诉恶魔本局可用伪装身份。\n首夜恶魔不进行击杀。`,
      tell: shouldGiveFirstNightEvilInfo ? `${state.language === "en" ? "Minions" : "爪牙"}：${minionNames}\n${state.language === "en" ? "Bluffs" : "可用伪装身份"}：${bluffNames}` : null,
      explain: shouldGiveFirstNightEvilInfo ? (state.language === "en" ? "The Demon receives Minion info and bluffs, but does not kill on night 1." : "首夜恶魔需要知道爪牙是谁，并获得 3 个伪装身份。首夜不进行击杀。") : (state.language === "en" ? "Small games skip first-night Demon info." : "小局跳过首夜恶魔信息。"),
      tip: state.language === "en" ? "Show Minions and bluffs. No kill tonight." : "展示爪牙和伪装身份，首夜不杀人。",
      display: demon && shouldGiveFirstNightEvilInfo ? { title: state.language === "en" ? "Demon info" : "恶魔信息", primary: state.language === "en" ? "Minions" : "爪牙", secondary: `${minionNames}\n\n${state.language === "en" ? "Bluffs" : "伪装"}：${bluffNames}`, footer: state.language === "en" ? "No kill on night 1." : "首夜不进行击杀。" } : null,
    }),
    ctPoisonerStep(),
    ctRedHerringStep(),
    ctWasherwomanStep(),
    ctLibrarianStep(),
    ctInvestigatorStep(),
    ctClockmakerStep(),
    ctChefStep(),
    ctEmpathStep(),
    ctChambermaidStep(),
    ctFortuneTellerStep(),
    ctButlerStep(),
    ctSpyStep(),
  ]);
}

function ctLaterNightSteps() {
  return ctFilterNightSteps([
    ctPoisonerStep(),
    ctUndertakerStep(),
    ctButlerStep(),
    ctEmpathStep(),
    ctChambermaidStep(),
    ctFortuneTellerStep(),
    ctDemonKillStep(),
    ctSageStep(),
    ctRavenkeeperStep(),
  ]);
}

function ctInfoStep(roleId, tell, explain, display, control, options = {}) {
  const actor = ctRoleActor(roleId);
  const title = nameOfRole(roleId);
  const poisonedNote = actor && ctActorIsPoisoned(actor)
    ? (state.language === "en" ? "Note: this player is poisoned. Their ability may give unreliable or false information." : "注意：这名玩家中毒，能力信息可以不可靠或错误。")
    : "";
  const drunkShownNote = actor && actor.actualRole === "drunk" && actor.shownRole === roleId
    ? (state.language === "en"
      ? `Note: this player is the Drunk shown as ${title}. Wake them normally, but the information may be unreliable or completely false.`
      : `注意：这名玩家真实身份是酒鬼，显示为${title}。请照常唤醒并给信息，但信息可以不可靠或完全错误。`)
    : "";
  const unreliable = actor && ctRoleUnreliable(roleId, actor);
  const displayOptions = unreliable && options.unreliableOptions ? options.unreliableOptions(actor) : [];
  return ctStep({
    title,
    actor,
    real: !!actor,
    reason: ctMissingReason(roleId),
    actionText: actor
      ? [
        state.language === "en" ? `Wake ${ctSeatLabel(actor)}. Give the information, then signal them to close their eyes.` : `轻拍 ${ctSeatLabel(actor)}，示意他睁眼。\n给出今晚的信息。\n确认后示意他闭眼。`,
        drunkShownNote,
        poisonedNote,
      ].filter(Boolean).join("\n")
      : (state.language === "en" ? "Pause briefly to preserve the night rhythm, then continue." : "为了避免泄露信息，请停顿 2-3 秒，然后点击下一步。"),
    tell: actor && !displayOptions.length ? tell : null,
    explain: [explain, drunkShownNote, poisonedNote].filter(Boolean).join("\n"),
    display: actor && !displayOptions.length && display ? display : null,
    displayOptions,
    control,
    headline: options.headline,
    tip: options.tip,
    roleId,
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
    roleId: "fortuneTeller",
  });
}

function ctStableHash(value) {
  return Array.from(String(value)).reduce((hash, ch) => ((hash * 31) + ch.charCodeAt(0)) >>> 0, 7);
}

function ctActorIsPoisoned(actor) {
  return !!actor && state.clocktower.poisonTarget === actor.id;
}

function ctRoleUnreliable(roleId, actor) {
  return !!actor && ((actor.actualRole === "drunk" && actor.shownRole === roleId) || ctActorIsPoisoned(actor));
}

function ctOrderedPair(first, second, key) {
  if (!first || !second) return [];
  return ctStableHash(key) % 2 === 0 ? [first, second] : [second, first];
}

function ctSeatNumbers(players) {
  return players.map((player) => player.seat);
}

function ctPairLabel(players) {
  return players.map(ctSeatLabel).join(" / ");
}

function ctScriptRoleIdsByTeam(team) {
  return clocktowerRolesForScript(state.clocktower.scriptId || "troubleBrewing")
    .filter((role) => role.team === team)
    .map((role) => role.id);
}

function ctDisplayChoice(label, display) {
  return { label, display };
}

function ctPickTwoPlayers(pool, key) {
  if (pool.length < 2) return [];
  const sorted = [...pool].sort((a, b) => a.seat - b.seat);
  const start = ctStableHash(key) % sorted.length;
  return [sorted[start], sorted[(start + 1) % sorted.length]];
}

function ctFakeEitherOneOptions({ roleId, team, title, emptyText, actor, trueTarget }) {
  const roleIds = ctScriptRoleIdsByTeam(team);
  const roleA = roleIds[ctStableHash(`${roleId}-role-a`) % Math.max(roleIds.length, 1)] || roleId;
  const roleB = roleIds[(ctStableHash(`${roleId}-role-b`) + 1) % Math.max(roleIds.length, 1)] || roleA;
  const basePool = ctPlayers().filter((p) => p.id !== actor.id && p.id !== trueTarget?.id);
  const fallbackPool = ctPlayers().filter((p) => p.id !== actor.id);
  const pool = basePool.length >= 2 ? basePool : fallbackPool;
  const pairA = ctPickTwoPlayers(pool, `${roleId}-fake-a-${actor.id}`);
  const pairB = ctPickTwoPlayers([...pool].reverse(), `${roleId}-fake-b-${actor.id}`);
  const makeDisplay = (fakeRoleId, pair) => pair.length === 2
    ? {
      title,
      primary: nameOfRole(fakeRoleId),
      subhead: state.language === "en" ? "is one of these two players" : "在下面两位玩家之中",
      numbers: ctSeatNumbers(pair),
      footer: "",
    }
    : {
      title,
      primary: emptyText,
      footer: "",
    };
  return [
    ctDisplayChoice(state.language === "en" ? "Show option A" : "展示选项 A", makeDisplay(roleA, pairA)),
    ctDisplayChoice(state.language === "en" ? "Show option B" : "展示选项 B", makeDisplay(roleB, pairB)),
  ];
}

function ctFakeNumberOptions({ title, trueValue, maxValue, footer }) {
  const max = Math.max(0, maxValue);
  const values = Array.from({ length: max + 1 }, (_, index) => index).filter((value) => value !== Number(trueValue));
  const fallback = values.length ? values : [Number(trueValue)];
  const first = fallback[0];
  const second = fallback.length > 1 ? fallback[fallback.length - 1] : fallback[0];
  return [
    ctDisplayChoice(state.language === "en" ? "Show option A" : "展示选项 A", { title, primary: String(first), footer }),
    ctDisplayChoice(state.language === "en" ? "Show option B" : "展示选项 B", { title, primary: String(second), footer }),
  ];
}

function ctFakeRoleRevealOptions(target, title) {
  if (!target) return [];
  const roles = clocktowerRolesForScript(state.clocktower.scriptId || "troubleBrewing");
  const alternatives = roles.filter((role) => role.id !== target.actualRole);
  const first = alternatives[ctStableHash(`${target.id}-reveal-a`) % Math.max(alternatives.length, 1)] || getRole(target.actualRole);
  const second = alternatives[ctStableHash(`${target.id}-reveal-b`) % Math.max(alternatives.length, 1)] || first;
  return [first, second].map((role, index) => ctDisplayChoice(
    state.language === "en" ? `Show option ${index + 1}` : `展示选项 ${index + 1}`,
    {
      title,
      primary: ctSeatLabel(target),
      secondary: nameOfRole(role.id),
      footer: state.language === "en" ? "Storyteller-selected information." : "说书人选择的信息。",
    },
  ));
}

function ctFakeDemonPairOptions(actor, title) {
  if (!actor) return [];
  const pool = ctPlayers().filter((p) => p.id !== actor.id);
  return ["a", "b"].map((suffix, index) => {
    const pair = ctPickTwoPlayers(index ? [...pool].reverse() : pool, `${actor.id}-sage-${suffix}`);
    return ctDisplayChoice(
      state.language === "en" ? `Show option ${index + 1}` : `展示选项 ${index + 1}`,
      {
        title,
        primary: state.language === "en" ? "Demon" : "恶魔",
        subhead: state.language === "en" ? "is one of these two players" : "在下面两位玩家之中",
        numbers: ctSeatNumbers(pair),
        footer: state.language === "en" ? "Storyteller-selected information." : "说书人选择的信息。",
      },
    );
  });
}

function ctWasherwomanStep() {
  const actor = ctRoleActor("washerwoman");
  const target = ctPlayers().find((p) => teamOfRole(p.actualRole) === "townsfolk" && p.actualRole !== "washerwoman");
  const pair = target ? ctPair(target, actor ? [actor.id] : []) : null;
  const ordered = target && pair ? ctOrderedPair(target, pair, `washerwoman-${target.id}-${pair.id}`) : [];
  const roleName = target ? nameOfRole(target.actualRole) : "";
  const title = state.language === "en" ? "Washerwoman info" : "洗衣妇信息";
  const explain = state.language === "en" ? "The Washerwoman learns one of two players is a specific Townsfolk." : "洗衣妇会得知某个镇民在两名玩家之一中。";
  const tell = ordered.length ? `${roleName}\n${ctPairLabel(ordered)}` : null;
  return ctInfoStep("washerwoman", tell, explain, ordered.length ? { title, primary: roleName, subhead: state.language === "en" ? "is one of these two players" : "在下面两位玩家之中", numbers: ctSeatNumbers(ordered), footer: "" } : null, null, {
    unreliableOptions: (stepActor) => ctFakeEitherOneOptions({ roleId: "washerwoman", team: "townsfolk", title, emptyText: state.language === "en" ? "No Townsfolk" : "没有镇民", actor: stepActor, trueTarget: target }),
  });
}

function ctLibrarianStep() {
  const actor = ctRoleActor("librarian");
  const target = ctPlayers().find((p) => teamOfRole(p.actualRole) === "outsider");
  const pair = target ? ctPair(target, actor ? [actor.id] : []) : null;
  const ordered = target && pair ? ctOrderedPair(target, pair, `librarian-${target.id}-${pair.id}`) : [];
  const roleName = target ? nameOfRole(target.actualRole) : "";
  const title = state.language === "en" ? "Librarian info" : "图书管理员信息";
  const none = state.language === "en" ? "No Outsiders" : "没有异乡人";
  const explain = state.language === "en" ? "The Librarian learns an Outsider is one of two players, or that there are no Outsiders." : "图书管理员会得知某个异乡人在两名玩家之一中，或得知没有异乡人。";
  const tell = ordered.length ? `${roleName}\n${ctPairLabel(ordered)}` : `${none}.`;
  return ctInfoStep("librarian", tell, explain, ordered.length ? { title, primary: roleName, subhead: state.language === "en" ? "is one of these two players" : "在下面两位玩家之中", numbers: ctSeatNumbers(ordered), footer: "" } : { title, primary: none, footer: "" }, null, {
    unreliableOptions: (stepActor) => ctFakeEitherOneOptions({ roleId: "librarian", team: "outsider", title, emptyText: none, actor: stepActor, trueTarget: target }),
  });
}

function ctInvestigatorStep() {
  const actor = ctRoleActor("investigator");
  const target = ctPlayers().find((p) => teamOfRole(p.actualRole) === "minion");
  const pair = target ? ctPair(target, actor ? [actor.id] : []) : null;
  const ordered = target && pair ? ctOrderedPair(target, pair, `investigator-${target.id}-${pair.id}`) : [];
  const roleName = target ? nameOfRole(target.actualRole) : "";
  const title = state.language === "en" ? "Investigator info" : "调查员信息";
  const none = state.language === "en" ? "No Minions" : "没有爪牙";
  const explain = state.language === "en" ? "The Investigator learns a Minion is one of two players, or that there are no Minions." : "调查员会得知某个爪牙在两名玩家之一中，或得知没有爪牙。";
  const tell = ordered.length ? `${roleName}\n${ctPairLabel(ordered)}` : `${none}.`;
  return ctInfoStep("investigator", tell, explain, ordered.length ? { title, primary: roleName, subhead: state.language === "en" ? "is one of these two players" : "在下面两位玩家之中", numbers: ctSeatNumbers(ordered), footer: "" } : { title, primary: none, footer: "" }, null, {
    unreliableOptions: (stepActor) => ctFakeEitherOneOptions({ roleId: "investigator", team: "minion", title, emptyText: none, actor: stepActor, trueTarget: target }),
  });
}

function ctClockmakerStep() {
  const value = ctClockmakerNumber();
  const num = String(value);
  const title = state.language === "en" ? "Clockmaker info" : "钟表匠信息";
  const footer = state.language === "en" ? "Distance to the nearest Minion." : "恶魔到最近爪牙的距离。";
  return ctInfoStep("clockmaker", num, state.language === "en" ? "This number is the distance from the Demon to the nearest Minion." : "这个数字表示恶魔到最近爪牙相隔几步。", { title, primary: num, footer }, null, {
    unreliableOptions: () => ctFakeNumberOptions({ title, trueValue: value, maxValue: Math.floor(ctPlayers().length / 2), footer }),
  });
}

function ctChefStep() {
  const value = ctChefPairs();
  const num = String(value);
  const title = state.language === "en" ? "Chef info" : "厨师信息";
  const footer = state.language === "en" ? "Adjacent evil pairs." : "邪恶玩家相邻对数。";
  return ctInfoStep("chef", num, state.language === "en" ? "This number is how many pairs of evil players are sitting next to each other." : "这个数字表示有几对邪恶玩家相邻而坐。", { title, primary: num, footer: state.language === "en" ? `There are ${num} adjacent evil pairs.` : `有 ${num} 对邪恶玩家相邻。` }, null, {
    unreliableOptions: () => ctFakeNumberOptions({ title, trueValue: value, maxValue: ctPlayers().length, footer }),
  });
}

function ctEmpathStep() {
  const actor = ctRoleActor("empath");
  const value = actor ? ctLivingNeighbors(actor).filter(ctIsEvil).length : 0;
  const num = actor ? String(value) : "";
  const title = state.language === "en" ? "Empath info" : "共情者信息";
  const footer = state.language === "en" ? "Evil living neighbors." : "邪恶存活邻居数量。";
  return ctInfoStep("empath", num, state.language === "en" ? "This number is how many of the Empath's two living neighbors are evil." : "这个数字表示共情者两个存活邻居中有几个邪恶玩家。", actor ? { title, primary: num, footer: state.language === "en" ? `Your two living neighbors include ${num} evil players.` : `你的两个存活邻居中有 ${num} 个邪恶玩家。` } : null, null, {
    unreliableOptions: () => ctFakeNumberOptions({ title, trueValue: value, maxValue: 2, footer }),
  });
}

function ctChambermaidStep() {
  const actor = ctRoleActor("chambermaid");
  return ctInfoStep("chambermaid", null, state.language === "en" ? "The Chambermaid chooses two players and learns how many woke tonight due to their ability." : "侍女选择两名玩家，得知其中有几人今晚因自己的能力醒来。", null, "chambermaid", {
    headline: actor ? (state.language === "en" ? `Wake ${ctSeatLabel(actor)}` : `唤醒侍女：${ctSeatLabel(actor)}`) : undefined,
    tip: state.language === "en" ? "Let her point at two seats. Tap Check and show." : "让她选择两名玩家，点查询后直接展示数字。",
  });
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

function ctChambermaidDisplayCard() {
  return {
    title: state.language === "en" ? "Chambermaid" : "侍女",
    primary: String(ctChambermaidResult()),
    subhead: state.language === "en" ? "among these two players" : "在下面两位玩家之中",
    numbers: [ctSeatNumberById(state.clocktower.chambermaidFirst), ctSeatNumberById(state.clocktower.chambermaidSecond)].filter(Boolean),
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
    roleId: "spy",
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
    roleId: "imp",
  });
}

function ctSageStep() {
  const demonPoisoned = ctDemonPoisonedTonight();
  const death = !demonPoisoned && state.clocktower.pendingNightDeath ? ctPlayers().find((p) => p.id === state.clocktower.pendingNightDeath) : null;
  const actor = death && death.actualRole === "sage" ? death : null;
  const demon = ctPlayers().find((p) => ctTeam(p) === "demon");
  const pair = demon ? ctPair(demon) : null;
  const unreliable = actor && ctActorIsPoisoned(actor);
  const displayOptions = unreliable ? ctFakeDemonPairOptions(actor, state.language === "en" ? "Sage info" : "贤者信息") : [];
  return ctStep({
    title: nameOfRole("sage"),
    actor,
    real: !!actor && !!demon && !!pair,
    reason: actor ? "" : (state.language === "en" ? "The Sage was not killed by the Demon tonight." : "贤者今晚没有被恶魔杀死。"),
    actionText: actor ? (state.language === "en" ? `Wake the Sage. Show two players, one of whom is the Demon.${unreliable ? " This Sage is poisoned, so choose unreliable information." : ""}` : `唤醒贤者。展示两名玩家，其中一名是恶魔。${unreliable ? "这名贤者中毒，请选择不可靠信息。" : ""}`) : (state.language === "en" ? "Pause briefly, then continue." : "停顿 2-3 秒，然后继续。"),
    tell: actor && demon && pair && !unreliable ? `${ctSeatLabel(demon)} / ${ctSeatLabel(pair)}` : null,
    explain: state.language === "en" ? "If the Demon kills the Sage, the Sage learns the Demon is one of two players." : "贤者被恶魔杀死时，得知恶魔是两名玩家之一。",
    display: actor && demon && pair && !unreliable ? { title: state.language === "en" ? "Sage info" : "贤者信息", primary: state.language === "en" ? "Demon" : "恶魔", subhead: state.language === "en" ? "is one of these two players" : "在下面两位玩家之中", numbers: [demon.seat, pair.seat], footer: "" } : null,
    displayOptions,
    roleId: "sage",
  });
}

function ctRavenkeeperStep() {
  const death = !ctDemonPoisonedTonight() && state.clocktower.pendingNightDeath ? ctPlayers().find((p) => p.id === state.clocktower.pendingNightDeath) : null;
  const actor = death && death.actualRole === "ravenkeeper" ? death : null;
  const target = state.clocktower.ravenkeeperTarget ? ctPlayers().find((p) => p.id === state.clocktower.ravenkeeperTarget) : null;
  const unreliable = actor && ctActorIsPoisoned(actor);
  const displayOptions = unreliable ? ctFakeRoleRevealOptions(target, state.language === "en" ? "Ravenkeeper info" : "守鸦人信息") : [];
  const tell = target ? `${ctSeatLabel(target)} ${state.language === "en" ? "is" : "的角色是"}：\n${nameOfRole(target.actualRole)}` : null;
  return ctStep({
    title: nameOfRole("ravenkeeper"),
    actor,
    real: !!actor,
    reason: actor ? "" : (state.language === "en" ? "The Ravenkeeper did not die tonight." : "守鸦人今晚没有死亡，不需要唤醒。"),
    actionText: actor ? (state.language === "en" ? `Wake the Ravenkeeper. Let them choose a player and show that character.${unreliable ? " This Ravenkeeper is poisoned, so choose unreliable information." : ""}` : `唤醒守鸦人。让他选择一名玩家，并告诉他该玩家的角色。${unreliable ? "这名守鸦人中毒，请选择不可靠信息。" : ""}`) : (state.language === "en" ? "Pause briefly, then continue." : "为了保持夜晚节奏，请停顿 2-3 秒。"),
    tell: unreliable ? null : tell,
    explain: state.language === "en" ? "Only wake the Ravenkeeper if they died at night." : "只有守鸦人夜晚死亡时才唤醒他。",
    control: "ravenkeeperTarget",
    display: target && !unreliable ? { title: state.language === "en" ? "Ravenkeeper info" : "守鸦人信息", primary: ctSeatLabel(target), secondary: nameOfRole(target.actualRole), footer: state.language === "en" ? "This player's character." : "该玩家的角色。" } : null,
    displayOptions,
    roleId: "ravenkeeper",
  });
}

function ctDemonPoisonedTonight() {
  const demon = ctAlive().find((p) => ctTeam(p) === "demon");
  return !!demon && ctActorIsPoisoned(demon);
}

function ctPair(target, excludeIds = []) {
  const blocked = new Set([target.id, ...excludeIds]);
  let players = ctPlayers().filter((p) => !blocked.has(p.id));
  if (!players.length) players = ctPlayers().filter((p) => p.id !== target.id);
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

function ctClockmakerNumber() {
  const players = ctPlayers().sort((a, b) => a.seat - b.seat);
  const demonIndex = players.findIndex((p) => ctTeam(p) === "demon");
  const minionIndexes = players.map((p, index) => ctTeam(p) === "minion" ? index : -1).filter((index) => index >= 0);
  if (demonIndex < 0 || !minionIndexes.length) return 0;
  return Math.min(...minionIndexes.map((index) => {
    const clockwise = (index - demonIndex + players.length) % players.length;
    const counterClockwise = (demonIndex - index + players.length) % players.length;
    return Math.min(clockwise, counterClockwise);
  }));
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

function ctChambermaidWakeRoles() {
  if (state.clocktower.phase === "firstNight") {
    return new Set(["clockmaker", "investigator", "empath", "chambermaid"]);
  }
  const roles = new Set(["chambermaid", "empath", "poisoner", "fortuneTeller", "butler", "monk", "imp"]);
  if (state.clocktower.lastExecuted) roles.add("undertaker");
  const death = !ctDemonPoisonedTonight() && state.clocktower.pendingNightDeath ? ctPlayers().find((p) => p.id === state.clocktower.pendingNightDeath) : null;
  if (death?.actualRole === "ravenkeeper") roles.add("ravenkeeper");
  return roles;
}

function ctChambermaidResult() {
  const c = state.clocktower;
  const wakeRoles = ctChambermaidWakeRoles();
  return [c.chambermaidFirst, c.chambermaidSecond]
    .map((id) => ctPlayers().find((p) => p.id === id))
    .filter((p) => p && wakeRoles.has(p.actualRole)).length;
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
    if (target && target.alive) ctKill(target, state.language === "en" ? "night death" : "夜晚死亡", {
      type: "death",
      title: state.language === "en" ? "Night death" : "夜晚死亡",
      detail: ctSeatLabel(target),
    });
    if (target && target.actualRole === "klutz" && !c.outcome) {
      c.pendingKlutz = target.id;
      c.klutzChoice = null;
      c.klutzReturnToDawn = true;
      c.phase = "day";
      c.dayMode = "klutz";
    }
  } else {
    ctAddEvent({
      type: "death",
      title: state.language === "en" ? "No night death" : "夜晚无人死亡",
      detail: "",
      phase: "night",
      round: c.round,
    });
  }
  c.fortuneFirst = null;
  c.fortuneSecond = null;
  c.chambermaidFirst = null;
  c.chambermaidSecond = null;
  c.ravenkeeperTarget = null;
  if (!c.outcome && !c.pendingKlutz) c.phase = "dawn";
}

function ctKill(player, cause, options = {}) {
  const c = state.clocktower;
  player.alive = false;
  ctAddEvent({
    type: options.type || "death",
    title: options.title || (state.language === "en" ? "Player died" : "玩家死亡"),
    detail: options.detail || `${ctSeatLabel(player)}：${cause}`,
    playerIds: [player.id],
    phase: c.phase,
    round: c.round,
    meta: { cause },
  });
  if (player.actualRole === "poisoner" && c.poisonTarget) {
    const formerTarget = c.poisonTarget;
    c.poisonTarget = null;
    ctAddEvent({
      type: "status",
      title: state.language === "en" ? "Poison ended" : "中毒结束",
      detail: state.language === "en"
        ? `${ctSeatLabelById(formerTarget)} became healthy because the Poisoner died.`
        : `${ctSeatLabelById(formerTarget)} 因投毒者死亡而恢复健康。`,
      playerIds: [player.id, formerTarget],
    });
  }
  if (ctTeam(player) === "demon") {
    const aliveAfter = ctAlive().length;
    const scarlet = ctAlive().find((p) => p.actualRole === "scarletWoman");
    if (scarlet && aliveAfter >= 5) {
      scarlet.actualRole = "imp";
      ctAddEvent({
        type: "roleChange",
        title: state.language === "en" ? "Scarlet Woman becomes Demon" : "猩红女郎接替",
        detail: state.language === "en"
          ? `${ctSeatLabel(scarlet)} became the new Demon.`
          : `${ctSeatLabel(scarlet)} 接替成为新的恶魔。`,
        playerIds: [scarlet.id],
        phase: c.phase,
        round: c.round,
      });
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
    ctOutcome(
      state.language === "en" ? "Good wins" : "善良阵营获胜",
      state.language === "en" ? "There is no living Demon." : "当前没有存活的恶魔。",
    );
  } else if (alive.length <= 2) {
    ctOutcome(
      state.language === "en" ? "Evil wins" : "邪恶阵营获胜",
      state.language === "en" ? "Only two players live and the Demon still lives." : "只剩 2 名玩家且恶魔仍然存活。",
    );
  }
}

function ctResolveVirginNomination() {
  const c = state.clocktower;
  const nominee = c.nominee ? ctPlayers().find((p) => p.id === c.nominee) : null;
  const nominator = c.nominator ? ctPlayers().find((p) => p.id === c.nominator) : null;
  if (!nominee || !nominator || nominee.actualRole !== "virgin" || c.virginUsed) return false;
  c.virginUsed = true;
  if (ctActorIsPoisoned(nominee) || teamOfRole(nominator.actualRole) !== "townsfolk") return false;

  c.lastExecuted = nominator.id;
  if (nominator.actualRole === "saint") {
    nominator.alive = false;
    ctAddEvent({
      type: "execution",
      title: state.language === "en" ? "Virgin execution" : "处女能力处决",
      detail: ctSeatLabel(nominator),
      playerIds: [nominee.id, nominator.id],
      phase: "day",
      round: c.round,
    });
    ctOutcome(
      state.language === "en" ? "Evil wins" : "邪恶阵营获胜",
      state.language === "en" ? "The Saint died by execution." : "圣徒死于处决，善良阵营失败。",
    );
    return true;
  }

  ctKill(nominator, state.language === "en" ? "executed by the Virgin ability" : "被处女能力处决", {
    type: "execution",
    title: state.language === "en" ? "Virgin execution" : "处女能力处决",
    detail: state.language === "en"
      ? `${ctSeatLabel(nominee)} was first nominated by Townsfolk ${ctSeatLabel(nominator)}. The nominator was executed.`
      : `${ctSeatLabel(nominee)} 首次被镇民 ${ctSeatLabel(nominator)} 提名，提名者立即被处决。`,
  });
  if (!c.outcome) startNextClocktowerNight();
  return true;
}

function confirmClocktowerDay() {
  const c = state.clocktower;
  const threshold = ctExecutionThreshold();
  const highestVoteIds = ctHighestVoteIds();
  const targetId = c.highestVoteCount >= threshold && highestVoteIds.length === 1 ? highestVoteIds[0] : null;
  if (!targetId) {
    ctAddEvent({
      type: "execution",
      title: state.language === "en" ? "No execution" : "无人处决",
      detail: "",
      phase: "day",
      round: c.round,
    });
    if (ctAlive().length === 3 && ctAlive().some((p) => p.actualRole === "mayor")) {
      ctOutcome(
        state.language === "en" ? "Good wins" : "善良阵营获胜",
        state.language === "en" ? "Mayor win condition: three alive and no execution." : "市长条件：只剩三名玩家且今天无人被处决。",
      );
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
      ctAddEvent({
        type: "execution",
        title: state.language === "en" ? "Execution" : "处决",
        detail: ctSeatLabel(target),
        playerIds: [target.id],
        phase: "day",
        round: c.round,
      });
      ctOutcome(
        state.language === "en" ? "Evil wins" : "邪恶阵营获胜",
        state.language === "en" ? "The Saint was executed." : "圣徒被处决，善良阵营失败。",
      );
      return;
    }
    ctKill(target, state.language === "en" ? "executed" : "被处决", {
      type: "execution",
      title: state.language === "en" ? "Execution" : "处决",
      detail: ctSeatLabel(target),
    });
    if (target.actualRole === "klutz" && !c.outcome) {
      c.pendingKlutz = target.id;
      c.klutzChoice = null;
      c.klutzReturnToDawn = false;
      c.dayMode = "klutz";
      return;
    }
  }
  if (!c.outcome) startNextClocktowerNight();
}

function confirmKlutzChoice() {
  const c = state.clocktower;
  const choice = c.klutzChoice ? ctPlayers().find((p) => p.id === c.klutzChoice) : null;
  const klutz = c.pendingKlutz ? ctPlayers().find((p) => p.id === c.pendingKlutz) : null;
  if (!choice || !klutz) return;
  ctAddEvent({
    type: "roleAction",
    title: state.language === "en" ? "Klutz choice" : "呆瓜选择",
    detail: `${ctSeatLabel(klutz)} → ${ctSeatLabel(choice)}`,
    playerIds: [klutz.id, choice.id],
    phase: "day",
    round: c.round,
  });
  if (ctIsEvil(choice)) {
    ctOutcome(
      state.language === "en" ? "Evil wins" : "邪恶阵营获胜",
      state.language === "en"
        ? `${ctSeatLabel(klutz)} chose an evil player: ${ctSeatLabel(choice)}.`
        : `${ctSeatLabel(klutz)} 选择了邪恶玩家 ${ctSeatLabel(choice)}，善良阵营失败。`,
    );
    return;
  }
  c.pendingKlutz = null;
  c.klutzChoice = null;
  if (c.klutzReturnToDawn) {
    c.klutzReturnToDawn = false;
    c.phase = "dawn";
    c.dayMode = "overview";
  } else {
    startNextClocktowerNight();
  }
}

function confirmArtistQuestion() {
  const c = state.clocktower;
  const claimant = c.artistClaimant ? ctPlayers().find((p) => p.id === c.artistClaimant) : null;
  if (!claimant) return;
  c.artistClaimantsUsed = Array.from(new Set([...(c.artistClaimantsUsed || []), claimant.id]));
  if (claimant.actualRole === "artist" && !c.artistUsed) c.artistUsed = true;
  ctAddEvent({
    type: "roleAction",
    title: state.language === "en" ? "Artist question" : "艺术家提问",
    detail: ctSeatLabel(claimant),
    playerIds: [claimant.id],
    phase: "day",
    round: c.round,
  });
  c.artistClaimant = null;
  c.dayMode = "overview";
}

function startNextClocktowerNight() {
  const c = state.clocktower;
  c.poisonTarget = null;
  c.round += 1;
  c.phase = "night";
  c.dayMode = "overview";
  c.nominator = null;
  c.nominee = null;
  c.voteCount = 0;
  c.highestVoteName = null;
  c.highestVoteCount = 0;
  c.highestVoteNames = [];
  c.selectedExecution = null;
  c.pendingNightDeath = null;
  c.lastNightDeath = null;
  c.artistClaimant = null;
}

function fireSlayer() {
  const c = state.clocktower;
  const claimant = ctPlayers().find((p) => p.id === c.slayerClaimant);
  const target = ctPlayers().find((p) => p.id === c.slayerTarget);
  if (!claimant || !target) return;
  c.slayerClaimantsUsed = Array.from(new Set([...(c.slayerClaimantsUsed || []), claimant.id]));
  const realFirstUse = claimant.actualRole === "slayer" && !c.slayerUsed;
  const abilityWorks = realFirstUse && !ctActorIsPoisoned(claimant);
  if (realFirstUse) c.slayerUsed = true;
  if (abilityWorks && ctTeam(target) === "demon") {
    ctKill(target, state.language === "en" ? "shot by Slayer" : "被猎手击杀", {
      type: "roleAction",
      title: state.language === "en" ? "Slayer killed the Demon" : "猎手击杀恶魔",
      detail: state.language === "en"
        ? `${ctSeatLabel(claimant)} used Slayer on ${ctSeatLabel(target)}. ${ctSeatLabel(target)} died.`
        : `${ctSeatLabel(claimant)} 对 ${ctSeatLabel(target)} 发动猎手能力，${ctSeatLabel(target)} 死亡。`,
    });
  } else {
    ctAddEvent({
      type: "roleAction",
      title: state.language === "en" ? "Slayer claim" : "猎手行动",
      detail: state.language === "en"
        ? `${ctSeatLabel(claimant)} claimed Slayer against ${ctSeatLabel(target)}. No one died.${realFirstUse && !abilityWorks ? " The ability was poisoned and is now spent." : ""}`
        : `${ctSeatLabel(claimant)} 声称猎手行动，目标 ${ctSeatLabel(target)}。没有玩家死亡。${realFirstUse && !abilityWorks ? "能力因中毒失效，但次数已经消耗。" : ""}`,
      playerIds: [claimant.id, target.id],
      phase: "day",
      round: c.round,
    });
  }
  c.slayerClaimant = null;
  c.slayerTarget = null;
  c.dayMode = "overview";
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
  if (action === "select-clocktower-script") state.selectedClocktowerScript = target.dataset.script;
  if (action === "start-clocktower") startClocktower();
  if (action === "reset-clocktower") state.clocktower = freshClocktower();
  if (action === "ct-test-start") ctStartTestGame();
  if (action === "ct-test-toggle") state.clocktower.testLabOpen = !state.clocktower.testLabOpen;
  if (action === "ct-test-load-role") {
    const select = document.querySelector("#ctTestRoleSelect");
    ctStartTestGame(select?.value || "empath");
  }
  if (action === "ct-test-scenario") ctLoadTestScenario(target.dataset.scenario);
  if (action === "ct-test-phase") {
    const c = state.clocktower;
    c.outcome = null;
    c.testNotice = "";
    c.phase = target.dataset.phase;
    c.round = c.phase === "firstNight" ? 1 : Math.max(2, c.round);
    c.nightStarted = c.phase !== "day";
    c.nightIndex = 0;
    c.dayMode = "overview";
  }
  if (action === "ct-test-step") {
    state.clocktower.testNotice = "";
    state.clocktower.nightStarted = true;
    state.clocktower.nightIndex = Number(target.dataset.index || 0);
  }
  if (action === "ct-test-alive") {
    const player = ctPlayers().find((p) => p.id === target.dataset.id);
    if (player) player.alive = !player.alive;
    state.clocktower.outcome = null;
  }
  if (action === "ct-test-poison") {
    const id = target.dataset.id;
    state.clocktower.poisonTarget = state.clocktower.poisonTarget === id ? null : id;
  }
  if (action === "ct-test-exit") state.clocktower = freshClocktower();
  if (action === "ct-toggle-grimoire") state.clocktower.grimoireOpen = state.clocktower.grimoireOpen === false;
  if (action === "ct-start-night") {
    const c = state.clocktower;
    c.nightStarted = true;
  }
  if (action === "ct-next-night") {
    const c = state.clocktower;
    const steps = ctNightSteps();
    ctRecordNightStep(steps[Math.min(c.nightIndex, steps.length - 1)]);
    if (c.nightIndex < steps.length - 1) c.nightIndex += 1;
    else finishClocktowerNight();
  }
  if (action === "ct-enter-day") {
    const c = state.clocktower;
    c.phase = "day";
    c.lastNightDeath = null;
    c.pendingNightDeath = null;
  }
  if (action === "ct-select") {
    const field = target.dataset.field;
    const id = target.dataset.id;
    state.clocktower[field] = state.clocktower[field] === id ? null : id;
    if (field === "fortuneFirst" && state.clocktower.fortuneSecond === state.clocktower.fortuneFirst) {
      state.clocktower.fortuneSecond = null;
    }
    if (field === "chambermaidFirst" && state.clocktower.chambermaidSecond === state.clocktower.chambermaidFirst) {
      state.clocktower.chambermaidSecond = null;
    }
    if (field === "slayerClaimant" && state.clocktower.slayerTarget === state.clocktower.slayerClaimant) {
      state.clocktower.slayerTarget = null;
    }
  }
  if (action === "ct-day-mode") {
    const c = state.clocktower;
    const nextMode = target.dataset.mode;
    let resolvedVirgin = false;
    if (nextMode === "vote" && c.nominator && c.nominee) {
      ctAddEvent({
        type: "nomination",
        title: state.language === "en" ? "Nomination" : "提名",
        detail: `${ctSeatLabelById(c.nominator)} → ${ctSeatLabelById(c.nominee)}`,
        playerIds: [c.nominator, c.nominee],
        phase: "day",
        round: c.round,
      });
      resolvedVirgin = ctResolveVirginNomination();
    }
    if (!resolvedVirgin) {
      c.dayMode = nextMode;
      if (nextMode === "vote") c.voteCount = ctExecutionThreshold();
    }
  }
  if (action === "ct-vote-minus") state.clocktower.voteCount = Math.max(0, state.clocktower.voteCount - 1);
  if (action === "ct-vote-plus") state.clocktower.voteCount = Math.min(ctAlive().length, state.clocktower.voteCount + 1);
  if (action === "ct-save-vote" || action === "ct-end-after-vote") {
    const c = state.clocktower;
    const threshold = ctExecutionThreshold();
    ctAddEvent({
      type: "vote",
      title: state.language === "en" ? "Vote result" : "投票结果",
      detail: `${ctSeatLabelById(c.nominee)} · ${c.voteCount}/${threshold}`,
      playerIds: c.nominee ? [c.nominee] : [],
      phase: "day",
      round: c.round,
      meta: { voteCount: c.voteCount, threshold },
    });
    if (c.voteCount >= threshold && c.voteCount > c.highestVoteCount) {
      c.highestVoteName = c.nominee;
      c.highestVoteNames = c.nominee ? [c.nominee] : [];
      c.highestVoteCount = c.voteCount;
    } else if (c.voteCount >= threshold && c.voteCount === c.highestVoteCount && c.nominee) {
      const tiedIds = ctHighestVoteIds();
      if (!tiedIds.includes(c.nominee)) tiedIds.push(c.nominee);
      c.highestVoteNames = tiedIds;
      c.highestVoteName = tiedIds.length === 1 ? tiedIds[0] : null;
    }
    c.dayMode = action === "ct-save-vote" ? "overview" : "endConfirm";
  }
  if (action === "ct-confirm-day") confirmClocktowerDay();
  if (action === "ct-confirm-klutz") confirmKlutzChoice();
  if (action === "ct-confirm-artist") confirmArtistQuestion();
  if (action === "ct-fire-slayer") fireSlayer();
  if (action === "show-display") {
    state.displayCard = JSON.parse(decodeURIComponent(target.dataset.card));
    ctRecordDisplayShown(state.displayCard);
  }
  if (action === "ct-show-fortune") {
    state.displayCard = ctFortuneDisplayCard();
    ctRecordDisplayShown(state.displayCard);
  }
  if (action === "ct-show-chambermaid") {
    state.displayCard = ctChambermaidDisplayCard();
    ctRecordDisplayShown(state.displayCard);
  }
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

document.addEventListener("change", (event) => {
  const target = event.target.closest("[data-change]");
  if (!target) return;
  if (target.dataset.change === "ct-test-role") {
    const player = ctPlayers().find((p) => p.id === target.dataset.id);
    if (player) {
      player.actualRole = target.value;
      player.shownRole = target.value === "drunk" ? "empath" : target.value;
      state.clocktower.outcome = null;
    }
  }
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
