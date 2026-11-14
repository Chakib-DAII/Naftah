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
				"/app/assets/jars/original-dependencies": [0, 100],
				"/app/assets/jars/lexer-literals": [0, 100],
				"/app/assets/jars/tokens-symbols.properties": [0, 100],
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

  // ASSIGNMENT
  assignment_basic: { title: "تعريف ثابت", code: `
--- تعريف ثابت "ا" بضرب عددين صحيحين
ثابت ا تعيين 1 * 100
ا
` },
  assignment_typed: { title: "تعريف ثابت من نوع عدد_طويل", code: `
--- تعريف ثابت "ذ" من نوع عدد_طويل (Long Integer) وتعيينه بقيمة أكبر من الحد الأعلى السابق
ثابت ذ : عدد_طويل_جدا تعيين 9223372036854775808
ذ
` },
  assignment_multiple: { title: "إعلان ثابت مع تعيين قيم مطابقة للتركيبة", code: `
--- إعلان ثابت مع تعيين قيم مطابقة للتركيبة (Tuple)
ثابت ت٫ش٬ع،ي تعيين "قبلي"؛400؛9223372036854775807؛(٣٢، ٤٥)
(ت٫ش٬ع،ي)` },
  assignment_typed_multiple: { title: "إعلان ثابت بأنواع متعددة وتعيين قيم مطابقة لها", code: `
--- إعلان ثابت بأنواع متعددة وتعيين قيم مطابقة لها
ثابت ت٫ش٬ع،ي: تسلسل_رموز؛عدد_طويل؛عدد_طويل؛أي_نوع تعيين "قبلي"؛400؛9223372036854775807؛(٣٢، ٤٥)
(ت٫ش٬ع،ي)` },

  // EXPRESSIONS
  expressions_basic: { title: "الجذر التربيعي", code: `
--- الجذر التربيعي لـ 4 = 2
"4" ** "0.5"
` },
  expressions_arithmetic: { title: "قائمة ضرب نفسها", code: `
--- قائمة ضرب نفسها
[٨٥، ٩٠، ٧٨، ٩٢] * [٨٥، ٩٠، ٧٨، ٩٢]
` },

  // CONDITIONALS
  if_basic: { title: "شرط بسيط", code: `
متغير حرارة تعيين 31
إذا حرارة أكبر_من 30 إذن {
  إطبع("الجو حار")
}
أنهي` },
  if_else: { title: "إذا وإلا", code: `
متغير نقاط تعيين 7
إذا نقاط أكبر_أو_يساوي 10 إذن {
  إطبع("ممتاز")
}
غير_ذلك {
  إطبع("تابع التدريب")
}
أنهي` },
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
  ternary_basic: { title: "تعبير ثلاثي", code: `
--- تعريف متغيرين بقيم ابتدائية
متغير أ تعيين ١
متغير ب تعيين 4

--- تعبير ثلاثي: إذا كانت أ صحيحة، أ، وإلا ب
أ ؟ أ : ب أنهي
` },
  ternary_nested: { title: "تعبير ثلاثي متداخل", code: `
--- تعريف متغيرين بقيم ابتدائية
متغير أ تعيين ١
متغير ب تعيين 4

(أ زائد ب أصغر_من ١٠) ؟ (أ ؟ أ : ب) : أ أنهي
` },
  nullish_basic: { title: "تعبير nullish", code: `
--- تعريف متغيرين بقيم ابتدائية
متغير أ تعيين ١
متغير ب تعيين 4

--- تعبير nullish: إذا كانت أ غير nullish، استخدم أ، وإلا ب
أ ؟؟ ب أنهي
` },
  nullish_nested: { title: "تعبير nullish متداخل", code: `
--- تعريف متغيرين بقيم ابتدائية
متغير أ تعيين ١
متغير ب تعيين 4
متغير ت تعيين 2
متغير ث تعيين 3
متغير ج تعيين 5

--- تعبير nullish متداخل
أ ؟؟ ب ؟؟ ت ؟؟ ث ؟؟ ج أنهي
` },
//
  // LOOPS
  loop_basic: { title: "حلقة تصاعدية", code: `
كرر_حلقة رقم تعيين 1 إلى 5 إفعل {
  إطبع(رقم)
}
أنهي` },
  loop_step: { title: "حلقة بخطوة", code: `
كرر_حلقة رقم تعيين 0 إلى 10 خطوة 2 إفعل {
  إطبع(رقم)
}
أنهي` },
  loop_reverse: { title: "حلقة تنازلية", code: `
كرر_حلقة عداد تعيين 5 تنازليا 1 إفعل {
  إطبع(عداد)
}
أنهي` },
  loop_nested: { title: "حلقات متداخلة", code: `
كرر_حلقة صف تعيين 1 إلى 3 إفعل {
  كرر_حلقة عمود تعيين 1 إلى 3 إفعل {
    إطبع((صف؛ عمود))
  }
  أنهي
}
أنهي` },
  loop_break: { title: "إيقاف حلقة", code: `
كرر_حلقة رقم تعيين 1 إلى 10 إفعل {
  إذا رقم يساوي 4 إذن { اكسر }
  أنهي
  إطبع(رقم)
}
أنهي` },
  loop_continue: { title: "تخطي دورة", code: `
كرر_حلقة رقم تعيين 1 إلى 6 إفعل {
  إذا رقم يساوي 3 إذن { تابع }
  أنهي
  إطبع(رقم)
}
أنهي` },
  loop_labeled_break: { title: "إيقاف حلقة مسماة", code: `
شبكة: كرر_حلقة صف تعيين 1 إلى 3 إفعل {
  كرر_حلقة عمود تعيين 1 إلى 3 إفعل {
    إذا (صف يساوي 2) و (عمود يساوي 2) إذن { اكسر شبكة }
    أنهي
    إطبع((صف؛ عمود))
  }
  أنهي
}
أنهي` },
  loop_array: { title: "المرور على قائمة", code: `
كرر_حلقة فاكهة ضمن ["تمر"؛ "رمان"؛ "برتقال"] إفعل {
  إطبع(فاكهة)
}
أنهي` },
  loop_range: { title: "فهرس وقيمة", code: `
كرر_حلقة فهرس؛ قيمة ضمن [10؛ 20؛ 30] إفعل {
  إطبع((فهرس؛ قيمة))
}
أنهي` },
  loop_map: { title: "المرور على خريطة", code: `
ثابت شخص تعيين \${"اسم": "سلمى"؛ "عمر": 30}
كرر_حلقة مفتاح: قيمة ضمن شخص إفعل {
  إطبع((مفتاح؛ قيمة))
}
أنهي` },
  loop_object: { title: "المرور على كائن", code: `
ثابت قيم تعيين ["نفطة"؛ "تونس"؛ "JVM"]
كرر_حلقة فهرس؛ قيمة ضمن قيم إفعل {
  إطبع("عنصر")
  إطبع((فهرس؛ قيمة))
}
أنهي` },

  // FUNCTIONS
  function_call: { title: "استدعاء دالة مدمجة", code: `
إطبع(إجمع(20؛ 22))` },
  function_declaration: { title: "تعريف دالة", code: `
دالة ضعف(ثابت رقم) {
  ارجع رقم ضارب 2
}
إطبع(ضعف(21))` },
  function_recursion: { title: "استدعاء ذاتي", code: `
دالة مجموع(ثابت رقم) {
  إذا رقم أصغر_أو_يساوي 0 إذن { ارجع 0 }
  أنهي
  ارجع رقم زائد مجموع(رقم ناقص 1)
}
إطبع(مجموع(5))` },
  function_factorial: { title: "مضروب عدد", code: `
دالة مضروب(ثابت رقم) {
  إذا رقم أصغر_أو_يساوي 1 إذن { ارجع 1 }
  أنهي
  ارجع رقم ضارب مضروب(رقم ناقص 1)
}
إطبع(مضروب(6))` },

  // IMPORT
  import_basic: { title: "استيراد دالة", code: `
إجلب إطبع مثل قل
قل("أهلاً من الاستيراد")` },
  import_rename: { title: "استيراد باسم جديد", code: `
إجلب إجمع مثل مجموع
إطبع(مجموع(8؛ 5))` },
  import_packages: { title: "استيراد نداء مؤهل", code: `
إجلب دوال:الحزم::إجمع مثل جمع_حزم
إطبع(جمع_حزم([1؛ 2]؛ [3؛ 4]))` },

  // JVM / JAVA INTEROP
  jvm_optional: { title: "Java Optional", code: `
ثابت اختياري تعيين جافا:أدة:اختياري::وف_فارغابل("نفطة")
إطبع(اختياري)` },
  jvm_string: { title: "إنشاء String من Java", code: `
ثابت نص تعيين جافا:لغة:سلسلة("نفطة")
إطبع(نص)` },
  jvm_list: { title: "إنشاء ArrayList من Java", code: `
ثابت قائمة_ تعيين جافا:أدة:صفائف_القائمة([1؛ 2؛ 3])
إطبع(قائمة_)` },
  jvm_object: { title: "إنشاء Object من Java", code: `
ثابت كائن_ تعيين جافا:لغة:كائن_()
إطبع(كائن_ لا_يساوي فارغ)` },

  // COLLECTIONS
  array_basic: { title: "الوصول إلى قائمة", code: `
ثابت قيم تعيين [10؛ 20؛ 30]
إطبع(دوال:الحزم::حصول_على_عنصر(قيم؛ 1))` },
  array_methods: { title: "تعديل قائمة", code: `
متغير قيم تعيين [10؛ 20؛ 30]
دوال:الحزم::تعيين_عنصر(قيم؛ 1؛ 99)
إطبع(قيم)` },
  object_basic: { title: "كائن بسيط", code: `
ثابت شخص تعيين @{ ثابت الاسم تعيين "سلمى"؛ ثابت العمر تعيين 30 }
إطبع(شخص:الاسم)` },
  object_iteration: { title: "خريطة بيانات", code: `
ثابت شخص تعيين \${"اسم": "سلمى"؛ "عمر": 30}
إطبع(شخص)` },

  // SWITCH
  switch_basic: { title: "اختيار حالات", code: `
ثابت يوم تعيين 2
اختر يوم من بين
1؛ 2: { إطبع("بداية الأسبوع") }
غير_ذلك { إطبع("يوم آخر") }` },
  switch_expression: { title: "اختيار بشرط", code: `
ثابت درجة تعيين 88
اختر درجة من بين
درجة أكبر_أو_يساوي 90: { إطبع("ممتاز") }
درجة أكبر_أو_يساوي 75: { إطبع("جيد جداً") }
غير_ذلك { إطبع("تابع التعلم") }` },

  // TRY / ERROR HANDLING
  try_basic: { title: "محاولة ناجحة", code: `
حاول(10 قسمة 2) {
  نجاح(نتيجة) -> إطبع(نتيجة)
  فشل(خطأ_) إفعل إطبع(خطأ_)
}` },
  try_catch: { title: "التقاط خطأ", code: `
حاول(10 قسمة 0) {
  نجاح(نتيجة) -> إطبع(نتيجة)
  فشل(خطأ_) إفعل إطبع("تم التقاط الخطأ")
}` },

  // CONCURRENCY
  concurrency_basic: { title: "دالة غير متزامنة", code: `
غير_متزامن دالة مهمة(ثابت اسم) {
  إطبع(اسم)
  ارجع اسم
}
ثابت شغل تعيين مهمة("مهمة قصيرة")
إطبع(انتظار شغل)` },
  concurrency_parallel: { title: "نطاق متواز", code: `
غير_متزامن دالة مهمة(ثابت اسم) {
  إطبع(اسم)
}
نطاق {
  مهمة("أ")
  مهمة("ب")
}` },

  // TEMPORAL
  time_basic: { title: "زمن عربي", code: `
زمن "١ يناير ٢٠٢٤ بالتقويم الميلادي ١٢:٠٠ صباحاً بتوقيت تونس"` },
  time_format: { title: "نقطة زمنية", code: `
نقطة_زمنية "٢٥ يوليو ٢٠٢١ بالتقويم الميلادي ٠٦:٣٠ مساءً +03:00"` },

  // RADIX / NUMBERS
  radix_binary: { title: "عدد ثنائي", code: `
ثابت قيمة تعيين '1010''2'
إطبع(قيمة)` },
  radix_hex: { title: "عدد سداسي عشري", code: `
ثابت لون تعيين 'FF00AA''16'
إطبع(لون)` },
  numbers_basic: { title: "حساب أعداد", code: `
ثابت سعر تعيين 19.99
ثابت كمية تعيين 3
إطبع(سعر ضارب كمية)` },

  // EXTRA ALGORITHMS
  algorithm_prime: { title: "توليد أعداد أولية", code: `
دالة أولي(ثابت رقم) {
  إذا رقم أصغر_من 2 إذن { ارجع خاطئ }
  أنهي
  متغير قاسم تعيين 2
  بينما قاسم * قاسم أصغر_أو_يساوي رقم افعل {
    إذا رقم % قاسم يساوي 0 إذن { ارجع رقم يساوي قاسم }
    أنهي
    زد قاسم
  }
  أنهي
  ارجع صحيح
}
متغير نتيجة تعيين []
كرر_حلقة رقم تعيين 1 إلى 30 إفعل {
  إذا أولي(رقم) إذن { نتيجة::إضافة(حجم(نتيجة)؛ رقم) }
  أنهي
}
أنهي
إطبع(نتيجة)` },
  algorithm_matrix: { title: "حساب مصفوفة", code: `
ثابت أ تعيين [[1؛ 2]؛ [3؛ 4]]
ثابت ب تعيين [[5؛ 6]؛ [7؛ 8]]
إطبع(دوال:الحزم::إجمع(أ؛ ب))` },
  algorithm_histogram: { title: "ضرب مصفوفة", code: `
ثابت درجات تعيين [10؛ 8؛ 10؛ 7؛ 8؛ 10]
إطبع(دوال:الحزم::إضرب(درجات؛ 10))
إطبع(دوال:الحزم::إضرب(درجات؛ 8))` },

  // EXTRA GAMES
  game_mastermind: { title: "ماسترمايند", code: `
ثابت السر تعيين ["أحمر"؛ "أزرق"؛ "أخضر"؛ "ذهبي"]
ثابت المحاولة تعيين ["أحمر"؛ "أبيض"؛ "أخضر"؛ "ذهبي"]
متغير صحيح_ تعيين 0
كرر_حلقة موضع تعيين 0 إلى 3 إفعل {
  إذا دوال:الحزم::حصول_على_عنصر(السر؛ موضع) يساوي دوال:الحزم::حصول_على_عنصر(المحاولة؛ موضع) إذن {
    زد صحيح_
  }
  أنهي
}
أنهي
إطبع("خانات صحيحة: {{صحيح_}}")` },
  game_blackjack: { title: "بلاك جاك", code: `
ثابت اللاعب تعيين [10؛ 7]
ثابت الموزع تعيين [10؛ 9]
ثابت مجموع_اللاعب تعيين 17
ثابت مجموع_الموزع تعيين 19
إذا مجموع_اللاعب أكبر_من مجموع_الموزع إذن {
  إطبع("فوز")
}
غير_ذلك_إذا مجموع_اللاعب يساوي مجموع_الموزع إذن {
  إطبع("تعادل")
}
غير_ذلك {
  إطبع("الموزع أقرب إلى 21")
}
أنهي` },
  game_treasure: { title: "كنز الشبكة", code: `
ثابت الحركات تعيين ["شرق"؛ "شرق"؛ "جنوب"؛ "جنوب"]
متغير س تعيين 0
متغير ص تعيين 0
كرر_حلقة حركة ضمن الحركات إفعل {
  إذا حركة يساوي "شرق" إذن { زد س }
  غير_ذلك { زد ص }
  أنهي
}
أنهي
إطبع((س؛ ص))` },

  // EXTRA JVM RECIPES
  jvm_math_game: { title: "رياضيات Java", code: `
ثابت قيمة تعيين جافا:لغة:الرياضيات::ابس(-42)
إطبع(قيمة)` },
  jvm_arrays: { title: "فرز مصفوفة Java", code: `
ثابت قيم تعيين [9؛ 3؛ 7؛ 1]
جافا:أدة:صفائف::هكذارت(قيم)
إطبع(قيم)` },
  jvm_objects: { title: "مقارنة كائنات Java", code: `
إطبع(جافا:أدة:الكائنات::متساوية("نفطة"؛ "نفطة"))` },

  // EXTRA LANGUAGE RECIPES
  pattern_menu: { title: "قائمة أوامر", code: `
متغير الأمر تعيين "ابدأ"
اختر الأمر من بين
"ابدأ": { إطبع("بدأت اللعبة") }
"توقف": { إطبع("توقفت اللعبة") }
غير_ذلك { إطبع("أمر غير معروف") }` },
  error_safe_math: { title: "حساب آمن", code: `
حاول(100 قسمة 0) {
  نجاح(نتيجة) -> إطبع(نتيجة)
  فشل(خطأ_) إفعل إطبع("القسمة على صفر غير مسموحة")
}` },
  nested_collections: { title: "قوائم متداخلة", code: `
ثابت خريطة تعيين [[1؛ 2]؛ [3؛ 4]؛ [5؛ 6]]
كرر_حلقة سطر ضمن خريطة إفعل {
  إطبع(سطر)
}
أنهي` },
  algorithm_average: { title: "حساب متوسط", code: `
ثابت درجات تعيين [16؛ 18؛ 14؛ 20]
متغير مجموع تعيين 0
كرر_حلقة درجة ضمن درجات إفعل {
  مجموع تعيين مجموع زائد درجة
}
أنهي
إطبع(مجموع قسمة حجم(درجات))` },
  game_scoreboard: { title: "لوحة نقاط", code: `
ثابت الجولات تعيين [3؛ 5؛ 2؛ 4]
متغير نقاط تعيين 0
كرر_حلقة نقطة ضمن الجولات إفعل {
  نقاط تعيين نقاط زائد نقطة
  إطبع(نقاط)
}
أنهي` },
  algorithm_palindrome: { title: "فحص تماثل", code: `
ثابت كلمة تعيين "نفتة"
ثابت معكوس تعيين "ةتفن"
إذا كلمة يساوي معكوس إذن {
  إطبع("متناظرة")
}
غير_ذلك {
  إطبع("ليست متناظرة")
}
أنهي` },
  tuple_data: { title: "بيانات مترابطة", code: `
ثابت لاعب تعيين ("ليلى"؛ 42؛ صحيح)
إطبع(لاعب)` },
  boolean_logic: { title: "منطق الشروط", code: `
ثابت جاهز تعيين صحيح
ثابت متصل تعيين صحيح
إذا جاهز و متصل إذن {
  إطبع("يمكن البدء")
}
أنهي` }

};

function loadExample(key) {
  const ex = examples[key];
  if (!ex) return;

  const textarea = document.getElementById("input");
  const newCode = ex.code || "";

  // Skip only if the content is identical
  if (textarea.value === newCode) return;

  textarea.value = newCode;

  // Force line-number update & reset scroll
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
  select.addEventListener("click", (e) => {
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