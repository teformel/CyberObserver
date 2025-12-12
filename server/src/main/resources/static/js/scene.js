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
    scene.background = new THREE.Color(0x020205);
    scene.fog = new THREE.FogExp2(0x020205, 0.02);

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
    bloomPass.threshold = 0.1;
    bloomPass.strength = 1.2; // Intense glow
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
    // Grid (Tron style)
    const gridHelper = new THREE.GridHelper(50, 50, 0x004400, 0x001100);
    scene.add(gridHelper);

    // Ambient Light
    const ambientLight = new THREE.AmbientLight(0x404040, 2);
    scene.add(ambientLight);

    // Directional Light
    const dirLight = new THREE.DirectionalLight(0xffffff, 1);
    dirLight.position.set(5, 10, 7);
    scene.add(dirLight);
}

function getOrCreateDeviceMesh(deviceId, type) {
    if (deviceMeshes[deviceId]) return deviceMeshes[deviceId];

    // Create Mesh based on type
    let geometry, material;

    if (type && type.includes('MOBILE')) {
        // Phone Shape
        geometry = new THREE.BoxGeometry(0.7, 1.4, 0.1);
        material = new THREE.MeshStandardMaterial({
            color: 0x00ffff,
            emissive: 0x004444,
            roughness: 0.2,
            metalness: 0.8
        });
    } else {
        // PC/Server Shape (Cube)
        geometry = new THREE.BoxGeometry(1, 1, 1);
        material = new THREE.MeshStandardMaterial({
            color: 0x00ff00,
            emissive: 0x004400,
            wireframe: true
        });
    }

    const mesh = new THREE.Mesh(geometry, material);
    mesh.position.y = 1; // Float above grid
    scene.add(mesh);

    // Add Label (Sprite) - Simplified for now

    deviceMeshes[deviceId] = mesh;
    return mesh;
}

export function updateDeviceState(status) {
    const mesh = getOrCreateDeviceMesh(status.deviceId, "UNKNOWN"); // Type should ideally come from status or registry

    if (status.sensorData) {
        const q = status.sensorData;
        // Check if valid quaternion
        if (q.qW !== undefined) {
            const quat = new THREE.Quaternion(q.qX, q.qY, q.qZ, q.qW);
            // Smooth lerp could go here
            mesh.setRotationFromQuaternion(quat);
        }
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
    // renderer.render(scene, camera); // Use composer instead
    composer.render();
}
