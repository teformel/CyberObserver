import * as THREE from 'three';
import { OrbitControls } from 'three/addons/controls/OrbitControls.js';
import { EffectComposer } from 'three/addons/postprocessing/EffectComposer.js';
import { RenderPass } from 'three/addons/postprocessing/RenderPass.js';
import { UnrealBloomPass } from 'three/addons/postprocessing/UnrealBloomPass.js';

let scene, camera, renderer, composer, controls;
const deviceMeshes = {}; // Map<deviceId, Mesh>

export function initScene(container) {
    // 1. Scene
    scene = new THREE.Scene();
    // scene.background = new THREE.Color(0x020205); // Removed for transparency
    scene.background = null;
    scene.fog = new THREE.FogExp2(0xffffff, 0.02); // Light fog for light theme

    // 2. Camera
    camera = new THREE.PerspectiveCamera(60, window.innerWidth / window.innerHeight, 0.1, 1000);
    camera.position.set(0, 3, 6);

    // 3. Renderer
    renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true });
    renderer.setPixelRatio(window.devicePixelRatio);
    renderer.setSize(window.innerWidth, window.innerHeight);
    renderer.toneMapping = THREE.ReinhardToneMapping;
    container.appendChild(renderer.domElement);

    // 4. Post-Processing (Bloom)
    const renderScene = new RenderPass(scene, camera);
    const bloomPass = new UnrealBloomPass(new THREE.Vector2(window.innerWidth, window.innerHeight), 1.5, 0.4, 0.85);
    bloomPass.threshold = 0.2;
    bloomPass.strength = 0.6; // Reduced glow for light theme
    bloomPass.radius = 0.5;

    composer = new EffectComposer(renderer);
    composer.addPass(renderScene);
    composer.addPass(bloomPass);

    // 5. Controls
    controls = new OrbitControls(camera, renderer.domElement);
    controls.enableDamping = true;
    controls.dampingFactor = 0.05;
    controls.maxPolarAngle = Math.PI / 2;

    // 6. Environment
    addEnvironment();

    // 7. Loop
    animate();

    // Resize
    window.addEventListener('resize', onWindowResize);
}

function addEnvironment() {
    // Grid (White/Light style)
    const gridHelper = new THREE.GridHelper(50, 50, 0x888888, 0xcccccc);
    scene.add(gridHelper);

    // Ambient Light
    const ambientLight = new THREE.AmbientLight(0x404040, 2);
    scene.add(ambientLight);

    // Directional Light
    const dirLight = new THREE.DirectionalLight(0xffffff, 1);
    dirLight.position.set(5, 10, 7);
    scene.add(dirLight);
}

// --- Avatar System ---

// Helper to create limb
function createLimb(w, h, d, color) {
    const geo = new THREE.BoxGeometry(w, h, d);
    const mat = new THREE.MeshStandardMaterial({
        color: color,
        roughness: 0.2,
        metalness: 0.8,
        emissive: 0x001111
    });
    return new THREE.Mesh(geo, mat);
}

function createCyberAvatar() {
    const root = new THREE.Group();
    root.userData.isAvatar = true;

    const cyberColor = 0x00ffff;

    // 1. Torso
    const torso = createLimb(0.5, 0.7, 0.25, cyberColor);
    torso.position.y = 1.0; // Center of torso
    torso.name = "torso";
    root.add(torso);

    // 2. Head
    const head = createLimb(0.25, 0.3, 0.3, cyberColor);
    head.position.set(0, 0.55, 0); // Relative to torso
    head.name = "head";
    torso.add(head);

    // Visor
    const visorGeo = new THREE.BoxGeometry(0.2, 0.08, 0.05);
    const visorMat = new THREE.MeshBasicMaterial({ color: 0xff0055 });
    const visor = new THREE.Mesh(visorGeo, visorMat);
    visor.position.set(0, 0.02, 0.16);
    head.add(visor);

    // 3. Limbs (Pivoted)

    // Left Arm
    const lArmPivot = new THREE.Group();
    lArmPivot.position.set(-0.35, 0.3, 0); // Shoulder pos relative to torso
    torso.add(lArmPivot);
    const lArm = createLimb(0.15, 0.6, 0.15, cyberColor);
    lArm.position.y = -0.25; // Offset so pivot is at top
    lArmPivot.add(lArm);
    root.userData.lArm = lArmPivot;

    // Right Arm
    const rArmPivot = new THREE.Group();
    rArmPivot.position.set(0.35, 0.3, 0);
    torso.add(rArmPivot);
    const rArm = createLimb(0.15, 0.6, 0.15, cyberColor);
    rArm.position.y = -0.25;
    rArmPivot.add(rArm);
    root.userData.rArm = rArmPivot;

    // Left Leg
    const lLegPivot = new THREE.Group();
    lLegPivot.position.set(-0.15, -0.35, 0); // Hip pos relative to torso
    torso.add(lLegPivot);
    const lLeg = createLimb(0.18, 0.7, 0.18, cyberColor);
    lLeg.position.y = -0.35;
    lLegPivot.add(lLeg);
    root.userData.lLeg = lLegPivot;

    // Right Leg
    const rLegPivot = new THREE.Group();
    rLegPivot.position.set(0.15, -0.35, 0);
    torso.add(rLegPivot);
    const rLeg = createLimb(0.18, 0.7, 0.18, cyberColor);
    rLeg.position.y = -0.35;
    rLegPivot.add(rLeg);
    root.userData.rLeg = rLegPivot;

    root.userData.torso = torso;

    return root;
}

export function updateDeviceState(status) {
    if (!deviceMeshes[status.deviceId]) {
        // Init new avatar
        const mesh = createCyberAvatar();
        scene.add(mesh);
        deviceMeshes[status.deviceId] = mesh;

        // Arrange in circle if multiple? Or just center for now.
        // If it's the first one, put at 0,0,0
        if (Object.keys(deviceMeshes).length > 1) {
            mesh.position.x = 2; // Offset simple
        }
    }

    const avatar = deviceMeshes[status.deviceId];

    // Handle Posture / IK
    if (status.extras && status.extras.human_ik) {
        animatePosture(avatar, status.extras.human_ik);
    }
}

function animatePosture(avatar, state) {
    const { lArm, rArm, lLeg, rLeg, torso } = avatar.userData;

    // Smooth transitions would require lerping using delta time, 
    // for now we set rotation directly for instant feedback.

    if (state === "SLEEPING" || state === "FLAT_FACE_UP") {
        // Laying down
        avatar.rotation.x = -Math.PI / 2;
        avatar.position.y = 0.3;

        // Arms relaxed
        lArm.rotation.x = -0.2;
        rArm.rotation.x = -0.2;
        lLeg.rotation.x = 0;
        rLeg.rotation.x = 0;
    }
    else if (state === "SITTING" || state === "UPRIGHT_PORTRAIT") {
        // Sitting
        avatar.rotation.x = 0;
        avatar.position.y = 0;

        torso.position.y = 1.0;

        // Hips bend
        lLeg.rotation.x = -Math.PI / 2;
        rLeg.rotation.x = -Math.PI / 2;

        // Knees bend (if we had knees, but we assume simple stick legs for now or add knee pivot)
        // With simple boxes, legs stick out forward.

        // Arms
        lArm.rotation.x = 0;
        rArm.rotation.x = 0;
    }
    else {
        // Standing / Interacting
        avatar.rotation.x = 0;
        avatar.position.y = 0;

        lLeg.rotation.x = 0;
        rLeg.rotation.x = 0;

        // Idle Animation
        const time = Date.now() * 0.002;
        lArm.rotation.x = Math.sin(time) * 0.1;
        rArm.rotation.x = -Math.sin(time) * 0.1;
    }
}

function onWindowResize() {
    camera.aspect = window.innerWidth / window.innerHeight;
    camera.updateProjectionMatrix();
    renderer.setSize(window.innerWidth, window.innerHeight);
    composer.setSize(window.innerWidth, window.innerHeight);
}

function animate() {
    requestAnimationFrame(animate);
    controls.update();
    composer.render();
}
