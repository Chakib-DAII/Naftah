/**
 * Naftah Browser Playground Runtime
 * ----------------------------------
 *
 * This script bootstraps the Naftah programming language playground
 * inside the browser using CheerpJ.
 *
 * It provides:
 *
 * - Java ↔ JavaScript native bridge for CheerpJ
 * - Console output rendering for Naftah programs
 * - Runtime initialization and JAR loading
 * - Debug logging system controlled via Jekyll environment
 *
 * Environment
 * -----------
 * DEBUG is enabled when:
 *   window.NAFTAH_ENV !== "production"
 *
 * This value is injected by Jekyll using:
 *   {{ jekyll.environment }}
 *
 * Java Integration (CheerpJ natives)
 * ----------------------------------
 * The following native methods are exposed to Java:
 *
 * - sendToHTML(text)
 *   -> Writes runtime output to the browser console UI
 *
 * - sendToLog(text)
 *   -> Writes debug logs to browser console (DEBUG only)
 *
 * - nativeSetApplication(app)
 *   -> Receives Java application instance and stores it globally
 *
 * CheerpJ Configuration
 * ---------------------
 * - Preloads runtime class scanning metadata
 * - Passes Java system properties
 * - Loads Naftah JAR at runtime
 *
 * UI Behavior
 * -----------
 * - Output is appended to #console
 * - Ctrl + Enter triggers execution
 * - Run button executes current input code
 *
 * Execution Flow
 * --------------
 * 1. initRuntime() initializes CheerpJ
 * 2. Java runtime is loaded (naftah.jar)
 * 3. Java application instance is stored globally
 * 4. runNaftahCode() sends code to Java runtime
 * 5. Result is printed in UI console
 *
 * Notes
 * -----
 * - Logging is disabled in production builds
 * - Java runtime communicates via async native bindings
 * - Designed specifically for browser + CheerpJ environment
 */

const RUNTIME_STATE = Object.freeze({
  BOOTSTRAPPING: "BOOTSTRAPPING",
  READY: "READY",
  FAILED: "FAILED"
});

const LOG_LEVELS = Object.freeze({
  TRACE: "TRACE",
  DEBUG: "DEBUG",
  INFO: "INFO",
  WARN: "WARN",
  ERROR: "ERROR"
});

const ENABLE_TRACE = false;

const USE_INDEX = true;
const SCAN_CLASS_PATH = false;
const CACHE_LOAD_ASYNC = true;

const LOG_LEVEL =
  (window.NAFTAH_ENV === "production")
    ? LOG_LEVELS.INFO
    : (ENABLE_TRACE ? LOG_LEVELS.TRACE : LOG_LEVELS.DEBUG);

/**
 * Logs debug messages to the browser console.
 *
 * Only prints when DEBUG is enabled (non-production environment).
 *
 * @param {...any} args - Values to log
 */
function log(...args) {
    console.log("[Naftah]", ...args);
}

const consoleEl = document.getElementById("console");

/**
 * Appends text to the on-screen playground console.
 *
 * Automatically scrolls to the bottom after writing.
 *
 * @param {string} text - Text to display in the UI console
 */
function writeConsole(text) {
    consoleEl.textContent += text;
    consoleEl.scrollTop = consoleEl.scrollHeight;
}

/**
 * Overrides CheerpJ default stdout handling.
 *
 * Decodes Java byte buffers into UTF-8 strings and logs them
 * to the browser console.
 *
 * @param {*} _ - Unused parameter (CheerpJ stream context)
 * @param {*} __ - Unused parameter (stream metadata)
 * @param {Uint8Array} buf - Byte buffer from Java stdout
 * @param {number} off - Start offset in buffer
 * @param {number} len - Number of bytes to read
 * @param {Function} cb - Callback to signal completion
 * @returns {*} CheerpJ callback result
 */
window.cheerpjDefaultConsoleWrite = function (_, __, buf, off, len, cb) {
    const text = new TextDecoder("utf-8")
        .decode(buf.slice(off, off + len));

    log(text);
    return cb(len);
};

/**
 * Receives output from Java and renders it in the UI console.
 *
 * Called automatically by CheerpJ when Java invokes:
 * NaftahPlayground.sendToHTML(...)
 *
 * @param {*} lib - CheerpJ runtime library context
 * @param {string} text - Output text from Java runtime
 * @returns {Promise<void>}
 */
async function Java_org_daiitech_naftah_playground_NaftahPlayground_sendToHTML(lib, text) {
    writeConsole(text + "\n");
}

/**
 * Receives debug logs from Java runtime and prints them
 * to browser console (only if DEBUG is enabled).
 *
 * @param {*} lib - CheerpJ runtime library context
 * @param {string} text - Log message from Java
 * @returns {Promise<void>}
 */
async function Java_org_daiitech_naftah_playground_NaftahPlayground_sendToLog(lib, text) {
    log(text);
}

const runButton = document.getElementById("run-btn");

/**
 * Receives the Java NaftahPlayground application instance.
 *
 * This allows JavaScript to call Java methods such as:
 * app.run(code)
 *
 * The returned Promise is intentionally never resolved
 * to keep the CheerpJ bridge alive.
 *
 * @param {*} lib - CheerpJ runtime library context
 * @param {object} app - Java application instance
 * @returns {Promise<never>}
 */
async function Java_org_daiitech_naftah_playground_NaftahPlayground_nativeSetApplication(lib, app) {
    window.naftahRuntimeApp = app;
    log("Java app instance received", window.naftahRuntimeApp);
	runButton.disabled = false;
    return new Promise(() => {}); // keep alive (as per CheerpJ docs pattern)
}

let runtimeState = RUNTIME_STATE.BOOTSTRAPPING;

function setBanner(state, text) {
  const banner = document.getElementById("runtime-banner");
  if (!banner) return;

  banner.className = `runtime-banner show ${state}`;
  banner.textContent = text;
}

function showBootstrapBanner() {
  showSpinner();   // show spinner
  setBanner(
    "bootstrapping",
    "⚠️ يتم الآن تشغيل بيئة نفطه… بعض الميزات قد لا تعمل بشكل كامل بعد."
  );
}

function setRuntimeReady() {
  runtimeState = RUNTIME_STATE.READY;

  hideSpinner();   // hide spinner
  setBanner(
    "ready",
    "✅ تم تشغيل بيئة نفطه بنجاح"
  );

  setTimeout(() => {
    const banner = document.getElementById("runtime-banner");
    if (!banner) return;

    banner.style.opacity = "0";

    setTimeout(() => {
      banner.remove();
    }, 250);
  }, 800);
}

function showErrorBanner() {
  runtimeState = RUNTIME_STATE.FAILED;

  hideSpinner();   // hide spinner
  setBanner(
    "error",
    "❌ فشل تشغيل بيئة نفطه. حاول إعادة تحميل الصفحة."
  );
}

async function Java_org_daiitech_naftah_playground_NaftahPlayground_setBootstrapState(lib, state) {
	switch (state) {
	case RUNTIME_STATE.BOOTSTRAPPING:
	  showBootstrapBanner();
	  break;

	case RUNTIME_STATE.READY:
	  setRuntimeReady();
  	  console.timeEnd("initRuntime");
	  break;

	case RUNTIME_STATE.FAILED:
	  showErrorBanner();
	  console.timeEnd("initRuntime");
	  break;

	default:
	  console.warn("[Naftah] Unknown runtime state:", state);
	}
}

async function Java_org_daiitech_naftah_playground_NaftahPlayground_loadIndexObject(lib) {
	return globalThis.CLASS_SCANNING_INDEX;
}

/**
 * Initializes the CheerpJ runtime environment.
 *
 * Responsibilities:
 * - Registers Java native bridges
 * - Sets Java system properties
 * - Preloads required Naftah resources
 * - Loads the Naftah JAR
 *
 * Also prints runtime diagnostics if DEBUG is enabled.
 *
 * @returns {Promise<void>}
 */
async function initRuntime() {
    try {
        log("Initializing...");

		console.time("initRuntime");

        await cheerpjInit({
			version: 17,

            natives: {
                Java_org_daiitech_naftah_playground_NaftahPlayground_sendToHTML,
                Java_org_daiitech_naftah_playground_NaftahPlayground_sendToLog,
                Java_org_daiitech_naftah_playground_NaftahPlayground_nativeSetApplication,
                Java_org_daiitech_naftah_playground_NaftahPlayground_setBootstrapState,
                Java_org_daiitech_naftah_playground_NaftahPlayground_loadIndexObject,
            },

			javaProperties: [
				`naftah.playground.log.level=${LOG_LEVEL}`,
				`naftah.scanClassPath=${SCAN_CLASS_PATH}`,
				`naftah.playground.index.active=${USE_INDEX}`,
				`naftah.playground.index.js.active=${globalThis.CLASS_SCANNING_INDEX != null}`,
				`naftah.cache.async=${CACHE_LOAD_ASYNC}`,
				`${SCAN_CLASS_PATH ? "naftah.cache.path=/app/assets/data/class-scanning-result.json.gz"
                   		   : (USE_INDEX ? "naftah.cache.index.minimal.path=/app/assets/data/minimal-class-scanning-index.json.gz"
                   		   				: "naftah.cache.minimal.path=/app/assets/data/minimal-class-scanning-result.json.gz")}`
			],

			preloadResources: {
				[SCAN_CLASS_PATH ? "/app/assets/data/class-scanning-result.json.gz"
						   : (USE_INDEX ? "/app/assets/data/minimal-class-scanning-index.json.gz"
						   				: "/app/assets/data/minimal-class-scanning-result.json.gz")
				] : [0, 100],
			},

			preloadProgress(done, total) {
				console.log(`Preloaded ${done}/${total}`);
			},

			status: LOG_LEVEL === LOG_LEVELS.DEBUG ? "none" : "default",
			enableDebug: LOG_LEVEL === LOG_LEVELS.DEBUG ? true : false
        });

        log("Loading JAR...");

        const jarUrl = "/app/assets/jars/naftah.jar";

        await cheerpjRunJar(jarUrl);

    } catch (err) {
        log("FAILED:", err);
    }
}
// TODO: add extra examples
const examples = {
  // BASIC
  hello_world: { title: "مرحباً أيها العالم", code: `إطبع("مرحباً أيها العالم!")` },
  hello_naftah: { title: "مرحباً نفطه", code: `إطبع("🌴 نفطه: واحة السحر والجمال
في قلب الصحراء التونسية، حيث تمتد الرمال الذهبية بلا نهاية، تقع مدينة نفطه، المعروفة بلقب 'الكوفة الصغيرة' . هذه المدينة ليست مجرد نقطة على الخريطة، بل هي لوحة فنية حية تنبض بالحياة والتاريخ.

🕌 مدينة الألف مسجد
نفطه ليست فقط واحة من النخيل، بل هي أيضًا مركز روحي هام. تضم أكثر من 24 مسجدًا و100 زاوية، مما يجعلها واحدة من أبرز مراكز التصوف في العالم الإسلامي. . إذا كنت من محبي الهدوء والتأمل، فزيارة هذه الأماكن ستأخذك في رحلة روحية عميقة.

🎬 نفطه في عالم السينما
هل تعلم أن نفطه كانت موقعًا لتصوير مشاهد من فيلم 'حرب النجوم'؟ نعم، تلك الكهوف الصحراوية التي ظهرت في الفيلم تقع في نفطه، مما يجعلها وجهة مميزة لعشاق السينما والخيال العلمي.

🏜️ مغامرة في الصحراء
إذا كنت من محبي المغامرة، فنفطه تقدم لك تجربة لا تُنسى. يمكنك ركوب الجمال عبر الكثبان الرملية، أو الاستمتاع بمشاهدة غروب الشمس الساحر من أعلى الكثبان.

🍽️ المأكولات التقليدية
لا تكتمل زيارة نفطه دون تذوق المأكولات التقليدية. جرب 'الكسكسي' المحضر بطرق محلية، أو 'الطاجين' مع التوابل الفريدة التي تشتهر بها المنطقة.

🌟 لماذا نفطه؟
نفطه ليست مجرد مدينة، بل هي تجربة ثقافية وروحية وطبيعية متكاملة. من تاريخها العريق إلى جمالها الطبيعي، ومن تقاليدها العميقة إلى ضيافة أهلها، تجعل من زيارتها رحلة لا تُنسى.")
` },

//  // ASSIGNMENT
//  assignment_basic: { title: "", code: `` },
//  assignment_typed: { title: "", code: `` },
//  assignment_multiple: { title: "", code: `` },
//
//  // EXPRESSIONS
//  expressions_basic: { title: "", code: `` },
//  expressions_arithmetic: { title: "", code: `` },
//
//  // CONDITIONALS
//  if_basic: { title: "", code: `` },
//  if_else: { title: "", code: `` },
  if_elseif: { title: "تعبير شرطي يحتوي على ثلاث حالات", code: `
--- تعريف متغيرين بقيم ابتدائية
متغير أ تعيين ١
متغير ب تعيين 4

---* تحقق من قيمة مجموع "أ + ب" باستخدام تعبير شرطي يحتوي على ثلاث حالات:
   - إذا كان الناتج أكبر من 10، اطبع رسالة بذلك.
   - إذا كان الناتج أصغر من 10، اطبع رسالة مناسبة.
   - إذا كان الناتج يساوي 10، اطبع رسالة مساوية.
*---
إذا أ زائد ب أكبر_من ١٠ إذن {
  --- في حال تحقق الشرط: مجموع أ + ب أكبر من 10
إطبع("أ زائد ب أكبر من 10")
} غير_ذلك_إذا أ زائد ب أصغر_من ١٠ إذن {
  --- في حال لم يتحقق الشرط الأول، وتحقق هذا الشرط: المجموع أصغر من 10
إطبع("أ زائد ب أصغر من 10")
} غير_ذلك {
  --- في حال لم يتحقق أي من الشرطين السابقين، أي أن أ + ب = 10
إطبع("أ زائد ب يساوي 10")
}
أنهي
` },
//  ternary_basic: { title: "", code: `` },
//  ternary_nested: { title: "", code: `` },
//  nullish_basic: { title: "", code: `` },
//  nullish_nested: { title: "", code: `` },
//
//  // LOOPS
//  loop_basic: { title: "", code: `` },
//  loop_step: { title: "", code: `` },
//  loop_reverse: { title: "", code: `` },
//  loop_nested: { title: "", code: `` },
//  loop_break: { title: "", code: `` },
//  loop_continue: { title: "", code: `` },
//  loop_labeled_break: { title: "", code: `` },
//
//  loop_array: { title: "", code: `` },
//  loop_range: { title: "", code: `` },
//  loop_map: { title: "", code: `` },
//  loop_object: { title: "", code: `` },
//
//  // FUNCTIONS
//  function_call: { title: "", code: `` },
//  function_declaration: { title: "", code: `` },
//  function_recursion: { title: "", code: `` },
//  function_factorial: { title: "", code: `` },
//
//  // IMPORT
//  import_basic: { title: "", code: `` },
//  import_rename: { title: "", code: `` },
//  import_packages: { title: "", code: `` },
//
//  // JVM / JAVA INTEROP
//  jvm_optional: { title: "", code: `` },
//  jvm_string: { title: "", code: `` },
//  jvm_list: { title: "", code: `` },
//  jvm_object: { title: "", code: `` },
//
//  // COLLECTIONS
//  array_basic: { title: "", code: `` },
//  array_methods: { title: "", code: `` },
//  object_basic: { title: "", code: `` },
//  object_iteration: { title: "", code: `` },
//
//  // SWITCH
//  switch_basic: { title: "", code: `` },
//  switch_expression: { title: "", code: `` },
//
//  // TRY / ERROR HANDLING
//  try_basic: { title: "", code: `` },
//  try_catch: { title: "", code: `` },
//
//  // CONCURRENCY
//  concurrency_basic: { title: "", code: `` },
//  concurrency_parallel: { title: "", code: `` },
//
//  // TEMPORAL
//  time_basic: { title: "", code: `` },
//  time_format: { title: "", code: `` },
//
//  // RADIX / NUMBERS
//  radix_binary: { title: "", code: `` },
//  radix_hex: { title: "", code: `` },
//  numbers_basic: { title: "", code: `` }

};

function loadExample(key) {
  const ex = examples[key];
  if (!ex) return;

  const textarea = document.getElementById("input");
  textarea.value = ex.code || "";

  // Force line‑number update & reset scroll
  updateLineNumbers();
  textarea.scrollTop = 0;
  syncLineNumberScroll();

  document.getElementById("console").textContent = "";
}

function initExamplesSelect() {
  const select = document.getElementById("examples-select");
  if (!select) return;

  // reset
  select.innerHTML = "";

  // placeholder
  const placeholder = document.createElement("option");
  placeholder.value = "";
  placeholder.textContent = "-- اختر مثال --";
  select.appendChild(placeholder);

  // build options
  for (const [key, ex] of Object.entries(examples)) {
    const option = document.createElement("option");

    option.value = key;

    // fallback order:
    // title → key
    option.textContent = (ex.title && ex.title.trim()) || key;

    select.appendChild(option);
  }

  // single clean handler
  select.addEventListener("change", (e) => {
    loadExample(e.target.value);
  });
}

document.addEventListener("DOMContentLoaded", () => {
  runButton.disabled = true;
  showSpinner();
  initExamplesSelect();
  updateLineNumbers();
});

function updateLineNumbers() {
  const textarea = document.getElementById('input');
  const lineNumbers = document.getElementById('line-numbers');
  if (!textarea || !lineNumbers) return;

  const lines = textarea.value.split('\n').length;
  // Create line numbers string (right-aligned within the div)
  let numbers = '';
  for (let i = 1; i <= lines; i++) {
    numbers += i + '\n';
  }
  lineNumbers.textContent = numbers;
}

function syncLineNumberScroll() {
  const textarea = document.getElementById('input');
  const lineNumbers = document.getElementById('line-numbers');
  if (!textarea || !lineNumbers) return;
  lineNumbers.scrollTop = textarea.scrollTop;
}

// Initialize and attach events
const textarea = document.getElementById('input');
if (textarea) {
  textarea.addEventListener('input', updateLineNumbers);
  textarea.addEventListener('scroll', syncLineNumberScroll);
  // Also update on resize (lines may be added/removed)
  window.addEventListener('resize', updateLineNumbers);
  new ResizeObserver(updateLineNumbers).observe(textarea);

  // Initial population
  updateLineNumbers();
}

const spinnerOverlay = document.getElementById('spinner-overlay');

function showSpinner() {
  if (spinnerOverlay) spinnerOverlay.classList.remove('hidden');
  runButton.disabled = true;
}

function hideSpinner() {
  if (spinnerOverlay) spinnerOverlay.classList.add('hidden');
  runButton.disabled = false;
}

/**
 * Executes Naftah source code through the Java runtime.
 *
 * Flow:
 * 1. Reads code from input field
 * 2. Sends it to Java via app.run(code)
 * 3. Displays result or error in console UI
 *
 * Requires Java runtime to be initialized.
 *
 * @returns {Promise<void>}
 */
async function runNaftahCode() {
    consoleEl.textContent = "";

    const code = document.getElementById("input").value;

    if (!window.naftahRuntimeApp || !code || !code.trim()) {
    	runButton.disabled = true;
        return;
    }

    try {
  		const result = await window.naftahRuntimeApp.run(code);
    } catch (err) {
        log("\n[Runtime Error]\n" + err);
    }
}

/**
 * Attaches event listeners for UI interactions:
 * - Run button click
 * - Ctrl + Enter shortcut
 */
window.runNaftahCode = runNaftahCode;

runButton.disabled = true;

runButton.addEventListener("click", () => {
    runNaftahCode();
});

document.addEventListener("keydown", (e) => {
    if (e.ctrlKey && e.key === "Enter") {
        runNaftahCode();
    }
});

/**
 * Entry point of the playground runtime.
 *
 * Initializes CheerpJ and starts the Naftah Java environment.
 */
await initRuntime();