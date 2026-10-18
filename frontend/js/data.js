/**
 * Suraksha - Sample Educational Data Store
 * Structured to align with future Spring Boot DTOs & JPA Entities.
 * All safety advice is aligned with NDMA & Red Cross emergency protocols.
 */

const SAMPLE_COURSES = [
  {
    id: "earthquake-101",
    title: "Earthquake Preparedness & Response",
    category: "EARTHQUAKE",
    categoryLabel: "Earthquake",
    difficulty: "BEGINNER",
    estimatedMinutes: 25,
    summary: "Master the Drop, Cover, and Hold On technique, identify classroom hazards, and prepare a 72-hour family survival kit.",
    lessonsCount: 4,
    quizId: "quiz-earthquake",
    drillId: "drill-earthquake",
    lessons: [
      {
        id: "eq-lesson-1",
        title: "The Science of Shaking & Early Warnings",
        content: `
          <p>Earthquakes occur when tectonic plates grinding against each other suddenly slip along a geological fault line. The energy released travels through the earth in seismic waves.</p>
          <p>Unlike weather hazards like cyclones, earthquakes strike without advance meteorological notice. However, modern seismic sensors can detect early <em>P-waves</em> (primary compressional waves) seconds before destructive <em>S-waves</em> arrive.</p>
          <h4>Key Hazard Takeaway:</h4>
          <p>Most earthquake casualties are not caused by the ground opening up; they are caused by collapsing non-structural elements: unanchored bookcases, heavy light fixtures, falling ceiling tiles, and shattering window glass.</p>
        `,
        dos: [
          "Secure tall furniture, lab shelves, and heavy electronics to wall studs with brackets.",
          "Identify safe shelter spots in every room: under sturdy desks, tables, or against interior walls.",
          "Keep an emergency battery-powered radio and flashlight near your bedside and classroom door."
        ],
        donts: [
          "Do not place heavy textbooks or glass trophies on high, open shelves.",
          "Do not rely on doorways for protection in modern buildings; they are rarely reinforced.",
          "Do not panic or rush blindly toward exits when shaking starts."
        ]
      },
      {
        id: "eq-lesson-2",
        title: "During Shaking: Drop, Cover, and Hold On",
        content: `
          <p>When the ground begins to violently shake, human balance is compromised within seconds. Attempting to run while floors are buckling frequently leads to broken limbs or head trauma from flying debris.</p>
          <h4>The Three Essential Steps:</h4>
          <ol style="margin-left: 1.5rem; line-height: 1.8;">
            <li><strong>DROP:</strong> Drop down immediately onto your hands and knees. This position protects you from being knocked over and allows you to stay low.</li>
            <li><strong>COVER:</strong> Cover your head and neck (and your entire body if possible) underneath a sturdy desk, bench, or heavy table. If no shelter is nearby, drop next to an interior wall and protect your head with your arms.</li>
            <li><strong>HOLD ON:</strong> Hold on to your shelter with one hand; be prepared to move with it if it shifts. Use your other arm to protect your neck and eyes.</li>
          </ol>
        `,
        dos: [
          "Drop, Cover, and Hold On until shaking completely stops.",
          "Turn your face away from windows to protect against shattering glass.",
          "If outdoors, move into an open area away from power lines, chimneys, and brick parapets."
        ],
        donts: [
          "DO NOT run outside while shaking is in progress. Falling masonry from building facades is deadly.",
          "DO NOT use elevators under any circumstances. Power outages will trap occupants.",
          "DO NOT push or crowd against classroom doors."
        ]
      },
      {
        id: "eq-lesson-3",
        title: "Post-Shaking Evacuation & Utility Safety",
        content: `
          <p>Once tremors subside, the immediate danger shifts to secondary hazards: gas leaks, electrical short circuits, fires, and aftershocks.</p>
          <p>Evacuate in a disciplined, single-file line following school evacuation routes to open assembly zones (such as playgrounds or football fields).</p>
          <h4>Detecting Secondary Hazards:</h4>
          <p>If you smell natural gas (a rotten-egg sulfur odor) or hear a hissing pipe: <strong>DO NOT turn on or off any electrical switches</strong>, light matches, or create sparks. Evacuate immediately and inform emergency safety officers.</p>
        `,
        dos: [
          "Check yourself and nearby peers for injuries and administer basic first aid if trained.",
          "Listen to official updates via battery-operated emergency transceivers.",
          "Wear sturdy closed-toe shoes to protect against shards of broken glass and debris."
        ],
        donts: [
          "Do not re-enter damaged buildings until certified safe by structural engineers.",
          "Do not operate electrical appliances or light switches if a gas leak is suspected.",
          "Do not spread unverified rumors on social media regarding aftershock predictions."
        ]
      },
      {
        id: "eq-lesson-4",
        title: "The 72-Hour Emergency Survival Kit",
        content: `
          <p>Following a major disaster, emergency responders may take up to 72 hours to reach all isolated neighborhoods. Every household and classroom should maintain a designated 'Go-Bag'.</p>
          <h4>Core Kit Checklist:</h4>
          <ul style="margin-left: 1.5rem; line-height: 1.7;">
            <li><strong>Drinking Water:</strong> 3 to 4 liters per person per day (for drinking and basic hygiene).</li>
            <li><strong>Non-Perishable Food:</strong> Energy bars, dried fruits, canned legumes, and a manual can opener.</li>
            <li><strong>Illumination & Power:</strong> LED flashlight, extra batteries, solar hand-crank charger.</li>
            <li><strong>First-Aid Essentials:</strong> Sterile bandages, antiseptic wipes, burn dressing, adhesive tape, personal medications.</li>
            <li><strong>Signaling & Tools:</strong> Loud whistle (audible through rubble), multi-tool, N95 dust masks.</li>
          </ul>
        `,
        dos: [
          "Inspect and refresh stored water and food supplies every 6 months.",
          "Store copies of identification, emergency contact cards, and prescriptions in a waterproof bag.",
          "Keep the emergency kit in an easily accessible location known to all family members."
        ],
        donts: [
          "Do not pack salty snacks that accelerate dehydration.",
          "Do not lock away the kit in inaccessible attics or basements.",
          "Do not rely solely on smartphone battery flashlights."
        ]
      }
    ]
  },
  {
    id: "flood-101",
    title: "Flood & Flash Flood Safety",
    category: "FLOOD",
    categoryLabel: "Flood",
    difficulty: "BEGINNER",
    estimatedMinutes: 20,
    summary: "Learn the danger of moving water, evacuation elevation tactics, water purification rules, and avoiding electrical hazards.",
    lessonsCount: 3,
    quizId: "quiz-flood",
    drillId: "drill-flood",
    lessons: [
      {
        id: "fl-lesson-1",
        title: "Flash Flood Dynamics & Early Warning",
        content: `
          <p>Flash floods are the most lethal type of weather disaster. They can develop within minutes of intense cloudbursts, dam releases, or riverbank breaches.</p>
          <p>Water has immense force: just <strong>15 cm (6 inches)</strong> of fast-moving water can knock an adult off their feet, and <strong>30 cm (12 inches)</strong> will float most compact vehicles.</p>
        `,
        dos: [
          "Move immediately to higher ground when a flash flood alert is issued; do not wait for instruction.",
          "Listen to meteorological bulletins and municipal siren systems.",
          "Identify community high-elevation shelter points before the monsoon season."
        ],
        donts: [
          "Never attempt to walk, swim, or drive through flooded roads (Turn Around, Don't Drown!).",
          "Never camp or park near dry stream beds, culverts, or drainage basins during rainfall.",
          "Never allow children to play in or near storm-water ditches."
        ]
      },
      {
        id: "fl-lesson-2",
        title: "Submerged Electrical Hazards & Sanitation",
        content: `
          <p>Flood waters frequently submerge electrical substations, downed utility poles, and street transformers, creating invisible electrified water fields.</p>
          <p>Furthermore, flood waters carry hazardous raw sewage, industrial runoff, chemical pesticides, and dangerous wildlife (such as snakes displaced from burrows).</p>
        `,
        dos: [
          "Turn off main electrical breakers and gas supplies before evacuating if safe to do so.",
          "Wear protective gumboots and use a wooden stick to probe ground stability if forced to wade.",
          "Disinfect all belongings and boil drinking water vigorously for at least 1 minute before consuming."
        ],
        donts: [
          "Do not touch electrical equipment or wiring if you are wet or standing in water.",
          "Do not drink unboiled municipal tap water after flood inundation.",
          "Do not consume food that has come into contact with floodwater."
        ]
      },
      {
        id: "fl-lesson-3",
        title: "Evacuation Protocol & Post-Flood Recovery",
        content: `
          <p>When evacuating during rising waters, preserve communication devices inside sealed plastic bags. Secure waterproof footwear, warm clothing, and critical documents.</p>
          <p>When returning to damaged structures, beware of weakened foundations, sagging ceilings, and trapped animal hazards.</p>
        `,
        dos: [
          "Follow marked official evacuation routes; shortcuts may lead to washed-out bridges.",
          "Ventilate flooded buildings thoroughly to prevent toxic black mold growth.",
          "Take photographs of structural damage for relief and insurance verification."
        ],
        donts: [
          "Do not cross bridges over fast-rushing rivers; foundation erosion can trigger sudden collapse.",
          "Do not turn on electrical fixtures until inspected by a licensed electrician.",
          "Do not wade barefoot through post-flood silt and mud."
        ]
      }
    ]
  },
  {
    id: "fire-101",
    title: "Structural Fire Prevention & Evacuation",
    category: "FIRE",
    categoryLabel: "Fire Safety",
    difficulty: "INTERMEDIATE",
    estimatedMinutes: 25,
    summary: "Understand combustion dynamics, master the PASS extinguisher technique, learn smoke-crawl egress, and prevent laboratory fires.",
    lessonsCount: 3,
    quizId: "quiz-fire",
    drillId: "drill-fire",
    lessons: [
      {
        id: "fr-lesson-1",
        title: "Fire Behavior & The PASS Protocol",
        content: `
          <p>Fire requires three elements to sustain combustion: <strong>Fuel</strong>, <strong>Oxygen</strong>, and <strong>Heat</strong> (the Fire Triangle). Extinguishing a fire requires removing at least one element.</p>
          <h4>The PASS Extinguisher Technique:</h4>
          <ul style="margin-left: 1.5rem; line-height: 1.7;">
            <li><strong>P - Pull:</strong> Pull the safety pin located at the top of the handle.</li>
            <li><strong>A - Aim:</strong> Aim the nozzle at the <em>base</em> of the fire, not at the flames.</li>
            <li><strong>S - Squeeze:</strong> Squeeze the lever slowly and evenly to discharge extinguishing agent.</li>
            <li><strong>S - Sweep:</strong> Sweep from side to side across the base until flames are smothered.</li>
          </ul>
        `,
        dos: [
          "Always keep your back to an unobstructed exit path when using an extinguisher.",
          "Use Class ABC dry chemical extinguishers for general classroom and electrical fires.",
          "Sound the building alarm immediately before attempting to fight any small fire."
        ],
        donts: [
          "Do not use water on electrical or oil/grease fires; it will cause explosive steam spread.",
          "Do not fight a fire if it has spread beyond a small wastebasket size; evacuate immediately.",
          "Do not let the fire block your only exit route."
        ]
      },
      {
        id: "fr-lesson-2",
        title: "Smoke Hazards: Crawl Low Under Smoke",
        content: `
          <p>In structural fires, <strong>toxic smoke kills more people than flames</strong>. Superheated smoke contains carbon monoxide, hydrogen cyanide, and soot particles that cause rapid asphyxiation within 2 to 3 breaths.</p>
          <p>Because hot smoke and toxic gases rise toward the ceiling, the coolest, most oxygenated air is found 30 to 60 cm (1 to 2 feet) above the floor.</p>
        `,
        dos: [
          "Drop on hands and knees and crawl low toward the nearest exit.",
          "Feel doors with the back of your hand before opening: if the door is hot, DO NOT open it.",
          "Seal door gaps with damp clothes or jackets if trapped inside a room, and signal from windows."
        ],
        donts: [
          "Do not stand upright or run through smoke-filled corridors.",
          "Do not open a hot door; doing so can cause an explosive 'backdraft'.",
          "Do not hide in closets or under beds where rescuers cannot locate you."
        ]
      },
      {
        id: "fr-lesson-3",
        title: "Emergency Evacuation & Assembly Safety",
        content: `
          <p>School and workplace fire drills establish muscle memory. When the fire alarm bells sound, stop all activities immediately and follow your designated evacuation monitor.</p>
        `,
        dos: [
          "Evacuate via stairs in single file, keeping one hand on the railing.",
          "Report immediately to your designated outdoor assembly zone for roll call.",
          "Close doors behind you as you leave to compartmentalize smoke and delay fire spread."
        ],
        donts: [
          "NEVER use elevators during a fire; electrical failure can trap you in the shaft.",
          "NEVER return inside a burning building to retrieve belongings, phones, or pets.",
          "Do not block fire hydrants or emergency vehicle access lanes."
        ]
      }
    ]
  },
  {
    id: "cyclone-101",
    title: "Cyclone & Severe Storm Mitigation",
    category: "CYCLONE",
    categoryLabel: "Cyclone",
    difficulty: "BEGINNER",
    estimatedMinutes: 20,
    summary: "Learn cyclone classification, storm surge risks, shelter securing methods, and surviving the deceptive calm eye of the storm.",
    lessonsCount: 3,
    quizId: "quiz-cyclone",
    drillId: "drill-cyclone",
    lessons: [
      {
        id: "cy-lesson-1",
        title: "Understanding Cyclones & Warning Signals",
        content: `
          <p>Tropical cyclones are severe rotating storm systems characterized by a low-pressure center, high-speed gale winds, torrential rains, and destructive coastal storm surges.</p>
          <p>Meteorological agencies issue color-coded alerts (Yellow: Watch, Orange: Alert, Red: Warning/Landfall Imminent). Take action early during the Orange alert stage.</p>
        `,
        dos: [
          "Inspect roofs, secure loose metal sheets, and trim weak tree branches near buildings.",
          "Board up or tape glass windows with heavy crisscross adhesive tape to prevent flying shards.",
          "Charge phones, power banks, and battery lights before grid power is shut down."
        ],
        donts: [
          "Do not ignore official evacuation orders if you reside within 5 km of the coastline.",
          "Do not leave outdoor livestock or pets tied up.",
          "Do not venture out to view ocean waves or storm surges."
        ]
      },
      {
        id: "cy-lesson-2",
        title: "The Eye of the Storm: Deceptive Calm",
        content: `
          <p>The 'Eye' is a roughly circular area of comparatively light winds and fair weather found at the center of a severe tropical cyclone.</p>
          <h4>Critical Survival Rule:</h4>
          <p>When the eye passes directly overhead, winds will abruptly drop to near-zero and the sky may clear. <strong>This is NOT the end of the storm!</strong> Within 30 to 45 minutes, the storm's opposite eyewall will strike with equal or greater intensity from the reverse direction.</p>
        `,
        dos: [
          "Remain inside your safe shelter even if the wind completely subsides.",
          "Keep tuned to official meteorological radio broadcasts.",
          "Stay in the strongest interior room of the building away from exterior glass."
        ],
        donts: [
          "DO NOT venture outside to inspect damage when winds suddenly cease.",
          "Do not touch downed power lines or tree branches tangled with wires.",
          "Do not turn on electrical breakers until the all-clear notice is broadcast."
        ]
      },
      {
        id: "cy-lesson-3",
        title: "Storm Surge & Post-Cyclone Safety",
        content: `
          <p>Storm surges—abnormal rises in sea level generated by atmospheric pressure and onshore winds—cause the vast majority of coastal cyclone deaths. Low-lying coastal terrain must be evacuated hours before landfall.</p>
        `,
        dos: [
          "Move to designated multi-story concrete cyclone shelters located on high ground.",
          "Drink only boiled or chemically treated chlorine water to avoid waterborne cholera outbreaks.",
          "Beware of snakes and dangerous insects seeking shelter in homes."
        ],
        donts: [
          "Do not enter waterlogged structures until checked for structural stability.",
          "Do not spread unverified voice notes or viral messages.",
          "Do not use open candles if gas leaks are suspected."
        ]
      }
    ]
  }
];

const SAMPLE_QUIZZES = {
  "quiz-earthquake": {
    courseId: "earthquake-101",
    title: "Earthquake Safety Certification Quiz",
    passingScorePercent: 75,
    questions: [
      {
        id: "q-eq-1",
        question: "When an earthquake strikes while you are inside a multi-story school building, what is the safest immediate action?",
        options: [
          { id: "opt-1", text: "Run down the stairs as fast as possible to reach the street" },
          { id: "opt-2", text: "Drop, Cover under a sturdy desk or table, and Hold On" },
          { id: "opt-3", text: "Take the nearest elevator to reach ground level" },
          { id: "opt-4", text: "Stand directly in the center of the room with arms raised" }
        ],
        correctOptionId: "opt-2",
        explanation: "Running during active shaking frequently causes falls and severe injuries from falling debris. Dropping under sturdy furniture shields your vital organs and head."
      },
      {
        id: "q-eq-2",
        question: "After earthquake shaking stops, you smell a strong odor of gas in the hallway. What should you do?",
        options: [
          { id: "opt-1", text: "Turn on the light switches to inspect the pipe" },
          { id: "opt-2", text: "Light a match to find the leak" },
          { id: "opt-3", text: "Evacuate immediately without operating any electrical switches" },
          { id: "opt-4", text: "Use your smartphone to record a video while standing near the pipe" }
        ],
        correctOptionId: "opt-3",
        explanation: "Flipping an electrical switch or lighting a flame creates microscopic electrical sparks that can ignite leaking gas and trigger an explosion."
      },
      {
        id: "q-eq-3",
        question: "How much drinking water per person per day should be stored in a family 72-hour disaster survival kit?",
        options: [
          { id: "opt-1", text: "0.5 liters per person" },
          { id: "opt-2", text: "1 to 2 cups total" },
          { id: "opt-3", text: "3 to 4 liters per person" },
          { id: "opt-4", text: "Water is unnecessary if you store soda" }
        ],
        correctOptionId: "opt-3",
        explanation: "Emergency disaster guidelines (NDMA & Red Cross) recommend 3 to 4 liters per person per day to cover hydration, food preparation, and minimal hygiene."
      },
      {
        id: "q-eq-4",
        question: "Why should you NOT stand under doorways for protection in modern buildings?",
        options: [
          { id: "opt-1", text: "Doorways are reserved exclusively for teachers" },
          { id: "opt-2", text: "Modern interior doorways are not reinforced and swinging doors can cause severe crush injuries" },
          { id: "opt-3", text: "Doorways are always electrified during earthquakes" },
          { id: "opt-4", text: "Doorways attract lightning" }
        ],
        correctOptionId: "opt-2",
        explanation: "In modern architecture, interior doorframes are no stronger than standard walls, and unsecured swinging doors frequently injure hands and fingers."
      }
    ]
  },
  "quiz-flood": {
    courseId: "flood-101",
    title: "Flood & Flash Flood Safety Quiz",
    passingScorePercent: 75,
    questions: [
      {
        id: "q-fl-1",
        question: "What depth of fast-moving floodwater is sufficient to knock an adult off their feet?",
        options: [
          { id: "opt-1", text: "15 cm (6 inches)" },
          { id: "opt-2", text: "60 cm (2 feet)" },
          { id: "opt-3", text: "1 meter" },
          { id: "opt-4", text: "Water cannot knock an adult over if they hold an umbrella" }
        ],
        correctOptionId: "opt-1",
        explanation: "Just 15 cm of swift-flowing water exerts hundreds of kilograms of force against human legs, making footing impossible to maintain."
      },
      {
        id: "q-fl-2",
        question: "What is the primary danger when approaching submerged roadways in a car during a flood?",
        options: [
          { id: "opt-1", text: "Washing the car too quickly" },
          { id: "opt-2", text: "Just 30 cm (12 inches) of moving water can float most cars and the road beneath may be completely washed out" },
          { id: "opt-3", text: "Water stains on car tires" },
          { id: "opt-4", text: "Tire slippage only on uphill roads" }
        ],
        correctOptionId: "opt-2",
        explanation: "Buoyancy displaces vehicle weight rapidly. 'Turn Around, Don't Drown' is the universal rule for flooded roadway crossings."
      },
      {
        id: "q-fl-3",
        question: "Why should you vigorously boil municipal tap water for at least 1 minute after flood inundation?",
        options: [
          { id: "opt-1", text: "To make it taste sweeter" },
          { id: "opt-2", text: "Floodwaters frequently breach sewage systems and contaminate water mains with lethal waterborne pathogens" },
          { id: "opt-3", text: "Warm water cools down the weather" },
          { id: "opt-4", text: "It is only required if the water has a bright color" }
        ],
        correctOptionId: "opt-2",
        explanation: "Submerged pipes and treatment plants often lose pressure, drawing in raw sewage, agricultural runoff, and dangerous bacteria like Vibrio cholerae."
      }
    ]
  },
  "quiz-fire": {
    courseId: "fire-101",
    title: "Fire Prevention & PASS Protocol Quiz",
    passingScorePercent: 75,
    questions: [
      {
        id: "q-fr-1",
        question: "In the PASS fire extinguisher technique, what does the letter 'A' stand for?",
        options: [
          { id: "opt-1", text: "Activate the ceiling sprinkler" },
          { id: "opt-2", text: "Aim at the base of the fire" },
          { id: "opt-3", text: "Aim at the top of the flames" },
          { id: "opt-4", text: "Ask someone for assistance" }
        ],
        correctOptionId: "opt-2",
        explanation: "Aiming at the base of the fire extinguishes the burning fuel. Aiming at the high flames simply blows chemicals through the air without extinguishing the source."
      },
      {
        id: "q-fr-2",
        question: "Why must you crawl low to the floor when escaping a smoke-filled room?",
        options: [
          { id: "opt-1", text: "To crawl past security cameras" },
          { id: "opt-2", text: "Hot toxic gases and deadly carbon monoxide rise; cool, breathable air remains 30-60 cm off the floor" },
          { id: "opt-3", text: "Floor carpets protect against burns" },
          { id: "opt-4", text: "Crawling makes you move faster than running" }
        ],
        correctOptionId: "opt-2",
        explanation: "Thermal buoyancy pushes hot soot, carbon monoxide, and hydrogen cyanide toward the ceiling. Inhaling superheated smoke can scorch lungs and cause unconsciousness."
      },
      {
        id: "q-fr-3",
        question: "Why should water NEVER be poured onto an electrical equipment fire?",
        options: [
          { id: "opt-1", text: "Water is too cold for computer chips" },
          { id: "opt-2", text: "Water conducts electricity, creating an immediate risk of fatal electrocution and spread" },
          { id: "opt-3", text: "Water creates annoying smoke" },
          { id: "opt-4", text: "Water evaporates too quickly" }
        ],
        correctOptionId: "opt-2",
        explanation: "Water contains dissolved minerals that conduct electrical current straight back to the person holding the container or nozzle."
      }
    ]
  },
  "quiz-cyclone": {
    courseId: "cyclone-101",
    title: "Cyclone & Severe Storm Safety Quiz",
    passingScorePercent: 75,
    questions: [
      {
        id: "q-cy-1",
        question: "During a severe tropical cyclone, the gale winds suddenly stop and the sky clears. What has occurred?",
        options: [
          { id: "opt-1", text: "The cyclone has permanently dissolved and it is safe to walk outside" },
          { id: "opt-2", text: "The eye of the storm is passing overhead; the opposite eyewall will strike shortly with equal fury" },
          { id: "opt-3", text: "A cold front has deflected the storm to another country" },
          { id: "opt-4", text: "Emergency sirens have dispersed the storm clouds" }
        ],
        correctOptionId: "opt-2",
        explanation: "The eye is a deceptive calm center. When it passes, intense eyewall winds will strike violently from the exact opposite direction."
      },
      {
        id: "q-cy-2",
        question: "What is the primary cause of fatalities during coastal cyclone landfall?",
        options: [
          { id: "opt-1", text: "Lightning strikes" },
          { id: "opt-2", text: "Coastal storm surges and inundation" },
          { id: "opt-3", text: "Hailstones" },
          { id: "opt-4", text: "Air pressure drops popping eardrums" }
        ],
        correctOptionId: "opt-2",
        explanation: "Storm surges can push seawater several kilometers inland at wall-like heights, engulfing low-lying buildings within minutes."
      }
    ]
  }
};

const SAMPLE_DRILLS = {
  "drill-earthquake": {
    id: "drill-earthquake",
    title: "Virtual Drill: High-Rise School Earthquake Simulation",
    hazard: "Earthquake",
    initialStepId: "step-1",
    steps: {
      "step-1": {
        id: "step-1",
        situation: "You are seated on the 3rd floor of your school during afternoon classes. Suddenly, the building violently rumbles. Books tumble off shelves, lights flicker, and floor tiles shudder.",
        question: "What is your immediate action?",
        choices: [
          {
            id: "choice-1a",
            text: "Sprint immediately for the hallway staircase to reach the ground floor.",
            outcomeType: "DANGER",
            feedback: "CRITICAL MISTAKE! Running while the building is shaking causes severe falls, and staircase stairwells often experience intense structural shear stress. Flying facade bricks outside the exit are lethal.",
            scoreImpact: -30,
            nextStepId: "step-2"
          },
          {
            id: "choice-1b",
            text: "Drop to your hands and knees under your sturdy wooden desk, Cover your head/neck, and Hold On.",
            outcomeType: "SAFE",
            feedback: "EXCELLENT DECISION! Dropping prevents falls. The sturdy desk shields your skull, spine, and eyes from falling fluorescent lights and shattered window fragments.",
            scoreImpact: 50,
            nextStepId: "step-2"
          },
          {
            id: "choice-1c",
            text: "Run toward the window to see what is happening in the courtyard.",
            outcomeType: "DANGER",
            feedback: "FATAL RISK! Windows shatter under seismic shear tension. Standing near glass exposes your body to high-speed flying glass shards.",
            scoreImpact: -40,
            nextStepId: "step-2"
          }
        ]
      },
      "step-2": {
        id: "step-2",
        situation: "The main shaking ceases after 45 seconds. Dust fills the air, fire alarms are ringing, and a strong rotten-egg (mercaptan gas) odor is detected near the hallway.",
        question: "What is your evacuation decision?",
        choices: [
          {
            id: "choice-2a",
            text: "Switch on the hallway light switch so everyone can see through the dust.",
            outcomeType: "DANGER",
            feedback: "DANGEROUS! Never touch electrical switches when gas is suspected. A tiny internal contact spark can ignite gas vapors and cause an explosion.",
            scoreImpact: -30,
            nextStepId: "step-3"
          },
          {
            id: "choice-2b",
            text: "Alert your teacher/peers of the gas odor, refrain from using any switches, and proceed calmly along the emergency stairwell.",
            outcomeType: "SAFE",
            feedback: "SPOT ON! You protected everyone from an ignition spark and initiated prompt, disciplined evacuation via stairs.",
            scoreImpact: 30,
            nextStepId: "step-3"
          }
        ]
      },
      "step-3": {
        id: "step-3",
        situation: "You reach the ground floor exit, but notice an elevator door slightly ajar with someone shouting for help inside. Tremors rumble again (an aftershock).",
        question: "How do you respond to the trapped person?",
        choices: [
          {
            id: "choice-3a",
            text: "Attempt to pry open the heavy elevator doors with your bare hands alone while standing under the shaft.",
            outcomeType: "DANGER",
            feedback: "UNSAFE! Elevator shafts can collapse or drop during aftershocks. Risking your life without specialized tools creates two victims instead of one.",
            scoreImpact: -20,
            nextStepId: "step-finish"
          },
          {
            id: "choice-3b",
            text: "Reassure the person loudly, tell them not to jump, exit immediately to the assembly zone, and alert the trained emergency response team.",
            outcomeType: "SAFE",
            feedback: "HEROIC & WISE! Emergency crews equipped with hydraulic spreaders can safely extricate trapped victims while you avoid being crushed during the aftershock.",
            scoreImpact: 20,
            nextStepId: "step-finish"
          }
        ]
      },
      "step-finish": {
        id: "step-finish",
        situation: "You have arrived safely at the open football field assembly zone. Roll call is underway, medical aid is arriving, and structural engineers are inspecting the building.",
        isFinal: true
      }
    }
  },
  "drill-fire": {
    id: "drill-fire",
    title: "Virtual Drill: Science Laboratory Fire Emergency",
    hazard: "Fire",
    initialStepId: "step-1",
    steps: {
      "step-1": {
        id: "step-1",
        situation: "During a chemistry experiment, an alcohol burner is tipped over. A pool of solvent catches fire on a lab bench, spreading toward paper notebooks.",
        question: "What is your immediate response?",
        choices: [
          {
            id: "choice-f1a",
            text: "Grab a water flask and throw water directly onto the flaming chemical solvent.",
            outcomeType: "DANGER",
            feedback: "DANGEROUS! Chemical solvents (like ethanol or oil) float on water. Splashing water will spread the flaming liquid across the floor!",
            scoreImpact: -30,
            nextStepId: "step-2"
          },
          {
            id: "choice-f1b",
            text: "Shout 'FIRE!', activate the wall alarm, and use a Class B/Dry Powder extinguisher if trained and exit is clear.",
            outcomeType: "SAFE",
            feedback: "CORRECT! Sounding the alarm warns the entire floor, and dry chemical powder smothers the chemical fire without spreading liquids.",
            scoreImpact: 50,
            nextStepId: "step-2"
          }
        ]
      },
      "step-2": {
        id: "step-2",
        situation: "Dense smoke starts rolling across the ceiling of the corridor outside the lab. Visibility is dropping and you begin coughing.",
        question: "How do you navigate through the corridor?",
        choices: [
          {
            id: "choice-f2a",
            text: "Drop to your hands and knees and crawl low toward the green illuminated emergency fire exit.",
            outcomeType: "SAFE",
            feedback: "PERFECT! Cool, breathable air stays near the ground. Crawling avoids asphyxiation from hot carbon monoxide.",
            scoreImpact: 40,
            nextStepId: "step-finish"
          },
          {
            id: "choice-f2b",
            text: "Stand tall and sprint as fast as possible with a deep breath.",
            outcomeType: "DANGER",
            feedback: "FATAL ERROR! Inhaling superheated smoke even once can induce laryngeal spasms and knock you unconscious within steps.",
            scoreImpact: -40,
            nextStepId: "step-finish"
          }
        ]
      },
      "step-finish": {
        id: "step-finish",
        situation: "You have successfully evacuated to the outside parking lot assembly point. The local fire brigade arrives within 5 minutes.",
        isFinal: true
      }
    }
  }
};

const SAMPLE_FIRST_AID = [
  {
    id: "cpr",
    title: "Hands-Only CPR (Cardiopulmonary Resuscitation)",
    category: "Life Support",
    disclaimer: "Hands-Only CPR is for adults who suddenly collapse. Always call 112 first before initiating compressions.",
    summary: "Immediate CPR can double or triple a cardiac arrest victim's chance of survival.",
    steps: [
      {
        num: 1,
        title: "Check Responsiveness & Call 112",
        detail: "Tap the victim firmly on the shoulder and shout 'Are you okay?'. If unresponsive and not breathing normally (or only gasping), shout for someone nearby to call 112 and bring an Automated External Defibrillator (AED)."
      },
      {
        num: 2,
        title: "Position Your Hands Correctly",
        detail: "Place the heel of one hand in the center of the victim's chest (between the nipples). Place your other hand on top and interlock your fingers. Keep your elbows locked and shoulders directly above hands."
      },
      {
        num: 3,
        title: "Push Hard and Fast",
        detail: "Compress the chest at a rate of 100 to 120 beats per minute (to the rhythm of the song 'Stayin' Alive'). Push down at least 5 cm (2 inches) deep, allowing the chest to fully recoil after each compression."
      },
      {
        num: 4,
        title: "Do Not Stop Until Help Arrives",
        detail: "Continue compressions without interruption until emergency medical responders arrive, an AED is ready to analyze, or the victim shows obvious signs of life (movement/breathing)."
      }
    ]
  },
  {
    id: "bleeding",
    title: "Severe External Bleeding Control",
    category: "Trauma Care",
    disclaimer: "Uncontrolled arterial bleeding can cause hemorrhagic shock within minutes. Call 112 immediately for spurting blood.",
    summary: "Immediate firm pressure stops over 90% of severe external bleeds.",
    steps: [
      {
        num: 1,
        title: "Apply Direct Firm Pressure",
        detail: "Place a sterile gauze pad or clean cloth directly over the bleeding wound. Press down firmly with both hands using your body weight."
      },
      {
        num: 2,
        title: "Do NOT Remove Soaked Dressings",
        detail: "If blood soaks completely through the cloth, DO NOT remove it—doing so disrupts newly formed blood clots. Layer additional clean pads directly over the first and keep pressing."
      },
      {
        num: 3,
        title: "Treat for Shock",
        detail: "Have the person lie down flat. Keep them warm with a blanket or jacket. Elevate their legs slightly (about 30 cm) if no spinal or pelvic injury is suspected."
      }
    ]
  },
  {
    id: "burns",
    title: "Thermal & Scald Burn Management",
    category: "Wound Care",
    disclaimer: "Seek immediate emergency hospital care for chemical burns, electrical burns, burns covering large areas, or facial burns.",
    summary: "Cool running water is the only proven method to stop thermal tissue destruction.",
    steps: [
      {
        num: 1,
        title: "Cool with Running Water for 20 Minutes",
        detail: "Immediately hold the burned area under cool (not freezing) running tap water for at least 20 minutes. This draws heat out of deeper dermal layers."
      },
      {
        num: 2,
        title: "NEVER Use Ice, Butter, or Toothpaste",
        detail: "Ice causes vasoconstriction and severe frostbite damage to burned skin. Oily substances like butter or toothpaste trap heat inside the flesh and promote serious infection."
      },
      {
        num: 3,
        title: "Cover Loosely with Clean Plastic Wrap or Gauze",
        detail: "Cover the burn loosely with sterile non-adherent dressing or clean plastic wrap. Do not burst blisters, as the blister roof protects against environmental bacteria."
      }
    ]
  },
  {
    id: "heatstroke",
    title: "Heat Exhaustion vs Heatstroke",
    category: "Environmental",
    disclaimer: "Heatstroke is a medical emergency with up to 50% mortality if untreated. Dial 112 immediately if the patient is confused or unconscious.",
    summary: "Differentiating pale/clammy heat exhaustion from life-threatening hot/dry heatstroke.",
    steps: [
      {
        num: 1,
        title: "Recognize the Difference",
        detail: "<strong>Heat Exhaustion:</strong> Heavy sweating, pale/clammy skin, dizziness, nausea.<br><strong>Heatstroke (EMERGENCY):</strong> Body temp > 40°C (104°F), hot red dry skin, slurred speech, confusion, seizures, loss of consciousness."
      },
      {
        num: 2,
        title: "Rapid Cooling for Heatstroke",
        detail: "Move the person to cool shade or air conditioning. Apply cold wet towels or ice packs to the neck, armpits, and groin where major blood vessels lie close to the skin."
      },
      {
        num: 3,
        title: "Hydration Guidelines",
        detail: "Only give sips of cool water if the person is alert and conscious. NEVER give liquids to an incoherent, confused, or vomiting patient due to choking risks."
      }
    ]
  }
];
