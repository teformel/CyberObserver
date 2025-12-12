#!/bin/bash
# Cyber Observer Management Script
# 混合模式：无参启动菜单，有参执行命令

# --- 配置 ---
LOG_DIR="logs"
mkdir -p "$LOG_DIR"

SERVER_PID_FILE="$LOG_DIR/server.pid"
PC_PID_FILE="$LOG_DIR/pc.pid"
SERVER_LOG="$LOG_DIR/server.log"
PC_LOG="$LOG_DIR/pc.log"

# --- 颜色 ---
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# --- 辅助函数 ---
log_info() { echo -e "${GREEN}[INFO]${NC} $1"; }
log_warn() { echo -e "${YELLOW}[WARN]${NC} $1"; }
log_err()  { echo -e "${RED}[ERR ]${NC} $1"; }

check_pid() {
    if [ -f "$1" ]; then
        pid=$(cat "$1")
        if kill -0 "$pid" 2>/dev/null; then
            return 0
        fi
    fi
    return 1
}

# --- 核心操作 ---

do_start_server() {
    if check_pid "$SERVER_PID_FILE"; then
        log_warn "Server is already running (PID: $(cat "$SERVER_PID_FILE"))"
    else
        log_info "Starting Server (Spring Boot)..."
        nohup mvn spring-boot:run -pl server > "$SERVER_LOG" 2>&1 &
        echo $! > "$SERVER_PID_FILE"
        log_info "Server started in background. Logs: $SERVER_LOG"
    fi
}

do_start_pc() {
    if check_pid "$PC_PID_FILE"; then
        log_warn "PC Client is already running (PID: $(cat "$PC_PID_FILE"))"
    else
        log_info "Starting PC Client..."
        nohup mvn exec:java -pl pc -Dexec.mainClass="com.cyber.pc.CyberPC" > "$PC_LOG" 2>&1 &
        echo $! > "$PC_PID_FILE"
        log_info "PC Client started in background. Logs: $PC_LOG"
    fi
}

do_stop() {
    local pid_file=$1
    local name=$2
    
    if [ -f "$pid_file" ]; then
        pid=$(cat "$pid_file")
        log_info "Stopping $name (PID: $pid)..."
        kill "$pid" 2>/dev/null
        pkill -P "$pid" 2>/dev/null
        rm "$pid_file"
        log_info "$name stopped."
    else
        log_warn "$name is not running."
    fi
}

do_status() {
    echo ""
    echo -e "${CYAN}--- Service Status ---${NC}"
    if check_pid "$SERVER_PID_FILE"; then
        echo -e "Server:    ${GREEN}● Running${NC} (PID: $(cat "$SERVER_PID_FILE"))"
    else
        echo -e "Server:    ${RED}● Stopped${NC}"
    fi

    if check_pid "$PC_PID_FILE"; then
        echo -e "PC Client: ${GREEN}● Running${NC} (PID: $(cat "$PC_PID_FILE"))"
    else
        echo -e "PC Client: ${RED}● Stopped${NC}"
    fi
    echo -e "${CYAN}----------------------${NC}"
    echo ""
}

# --- CLI 处理逻辑 ---
handle_cli() {
    MODE=$1
    TARGET=${2:-all}

    case "$MODE" in
        start)
            case "$TARGET" in
                server) do_start_server ;;
                pc)     do_start_pc ;;
                all)    do_start_server; do_start_pc ;;
                *)      echo "Usage: $0 start {server|pc|all}" ;;
            esac
            ;;
        stop)
            case "$TARGET" in
                server) do_stop "$SERVER_PID_FILE" "Server" ;;
                pc)     do_stop "$PC_PID_FILE" "PC Client" ;;
                all)    do_stop "$SERVER_PID_FILE" "Server"; do_stop "$PC_PID_FILE" "PC Client" ;;
                *)      echo "Usage: $0 stop {server|pc|all}" ;;
            esac
            ;;
        restart)
            $0 stop "$TARGET"
            sleep 2
            $0 start "$TARGET"
            ;;
        status)
            do_status
            ;;
        log)
            if [ "$TARGET" == "pc" ]; then
                tail -f "$PC_LOG"
            else
                tail -f "$SERVER_LOG"
            fi
            ;;
        help|-h|--help)
             echo "Usage: $0 {start|stop|restart|status|log} [server|pc|all]"
             ;;
        *)
            echo "Unknown command: $MODE"
            echo "Try '$0 --help' for usage."
            exit 1
            ;;
    esac
}

# --- 交互菜单逻辑 ---
show_menu() {
    while true; do
        clear
        echo -e "${CYAN}====== 🔮 Cyber Observer Manager ======${NC}"
        do_status
        echo "1. Start All     (启动所有)"
        echo "2. Stop All      (停止所有)"
        echo "3. Restart All   (重启所有)"
        echo "4. Tail Server Log (查看日志)"
        echo ""
        echo "5. Start Server Only"
        echo "6. Start PC Only"
        echo "0. Exit          (退出)"
        echo -e "${CYAN}=======================================${NC}"
        read -p "👉 Choose: " choice

        case $choice in
            1) do_start_server; do_start_pc; read -p "Press Enter..." ;;
            2) do_stop "$SERVER_PID_FILE" "Server"; do_stop "$PC_PID_FILE" "PC Client"; read -p "Press Enter..." ;;
            3) 
               do_stop "$SERVER_PID_FILE" "Server"; do_stop "$PC_PID_FILE" "PC Client"
               sleep 2
               do_start_server; do_start_pc
               read -p "Press Enter..." 
               ;;
            4) tail -f "$SERVER_LOG" ;;
            5) do_start_server; read -p "Press Enter..." ;;
            6) do_start_pc; read -p "Press Enter..." ;;
            0) exit 0 ;;
            *) echo "Invalid option"; sleep 1 ;;
        esac
    done
}

# --- 入口判断 ---

if [ -z "$1" ]; then
    # 无参数 -> 进入交互模式
    show_menu
else
    # 有参数 -> 进入 CLI 模式
    handle_cli "$@"
fi
