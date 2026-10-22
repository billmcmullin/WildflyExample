/* global Phaser */
const snakeDirections = {
    up: { column: 0, row: -1 },
    down: { column: 0, row: 1 },
    left: { column: -1, row: 0 },
    right: { column: 1, row: 0 }
};

class SnakeScene extends Phaser.Scene {
    constructor() {
        super('Snake');
        this.size = 20;
        this.cell = 24;
    }

    create() {
        this.board = this.add.graphics();
        this.best = 0;
        try {
            const saved = Number(localStorage.getItem('wildflyexample.snake.best'));
            if (Number.isSafeInteger(saved) && saved >= 0) this.best = saved;
        } catch (error) {
            this.best = 0;
        }
        this.timer = this.time.addEvent({ delay: 140, loop: true, callback: () => this.step() });
        document.getElementById('snake-toggle').addEventListener('click', () => this.toggle());
        document.getElementById('snake-restart').addEventListener('click', () => this.reset(true));
        document.getElementById('snake-speed').addEventListener('change', () => this.updateSpeed());
        document.querySelectorAll('[data-direction]').forEach(button => {
            button.addEventListener('click', () => this.turn(button.dataset.direction));
        });
        this.input.keyboard.on('keydown', event => {
            if (['INPUT', 'SELECT', 'TEXTAREA'].includes(event.target.tagName)) return;
            if (event.code === 'Space' && event.target.tagName === 'BUTTON') return;
            const keys = { ArrowUp: 'up', w: 'up', ArrowDown: 'down', s: 'down', ArrowLeft: 'left', a: 'left', ArrowRight: 'right', d: 'right' };
            const direction = keys[event.key] || keys[event.key.toLowerCase()];
            if (direction || event.code === 'Space') event.preventDefault();
            if (direction) this.turn(direction);
            if (event.code === 'Space') this.toggle();
        });
        this.game.events.on(Phaser.Core.Events.BLUR, () => {
            if (this.state === 'playing') this.setState('paused');
        });
        document.getElementById('snake-toggle').disabled = false;
        document.getElementById('snake-restart').disabled = false;
        this.reset(false);
    }

    reset(start) {
        this.snake = [{ column: 7, row: 10 }, { column: 6, row: 10 }, { column: 5, row: 10 }, { column: 4, row: 10 }];
        this.direction = 'right';
        this.turns = [];
        this.score = 0;
        this.placeFood();
        this.updateSpeed();
        this.setState(start ? 'playing' : 'ready');
        this.draw();
    }

    toggle() {
        if (this.state === 'playing') this.setState('paused');
        else if (this.state === 'paused' || this.state === 'ready') this.setState('playing');
        else this.reset(true);
    }

    setState(state) {
        this.state = state;
        const labels = { ready: 'Ready', playing: 'Playing', paused: 'Paused', over: 'Game over', won: 'Board cleared' };
        document.getElementById('snake-status').textContent = labels[state];
        document.getElementById('snake-overlay-title').textContent = labels[state];
        document.getElementById('snake-overlay').hidden = state === 'playing';
        document.getElementById('snake-toggle').textContent = state === 'playing' ? 'Pause' : state === 'paused' ? 'Resume' : 'Start';
        document.getElementById('snake-score').textContent = this.score;
        document.getElementById('snake-best').textContent = this.best;
        this.timer.elapsed = 0;
    }

    turn(direction) {
        if (this.state === 'over' || this.state === 'won' || this.state === 'paused') return;
        if (this.turns.length >= 2) return;
        const previous = snakeDirections[this.turns.length ? this.turns[this.turns.length - 1] : this.direction];
        const next = snakeDirections[direction];
        if (!next || (next.column === -previous.column && next.row === -previous.row)) return;
        this.turns.push(direction);
        if (this.state === 'ready') this.setState('playing');
    }

    step() {
        if (this.state !== 'playing') return;
        if (this.turns.length) this.direction = this.turns.shift();
        const movement = snakeDirections[this.direction];
        const head = { column: this.snake[0].column + movement.column, row: this.snake[0].row + movement.row };
        const eating = head.column === this.food.column && head.row === this.food.row;
        const body = eating ? this.snake : this.snake.slice(0, -1);
        if (head.column < 0 || head.row < 0 || head.column >= this.size || head.row >= this.size ||
                body.some(segment => segment.column === head.column && segment.row === head.row)) {
            this.setState('over');
            return;
        }
        this.snake.unshift(head);
        if (!eating) this.snake.pop();
        else {
            this.score += 10;
            this.best = Math.max(this.best, this.score);
            try {
                localStorage.setItem('wildflyexample.snake.best', String(this.best));
            } catch (error) {
                this.best = Math.max(this.best, this.score);
            }
            document.getElementById('snake-score').textContent = this.score;
            document.getElementById('snake-best').textContent = this.best;
            this.updateSpeed();
            if (this.snake.length === this.size * this.size) {
                this.food = null;
                this.setState('won');
            } else this.placeFood();
        }
        this.draw();
    }

    updateSpeed() {
        this.timer.delay = Math.max(60, Number(document.getElementById('snake-speed').value) - Math.floor(this.score / 20) * 3);
    }

    placeFood() {
        const empty = [];
        for (let row = 0; row < this.size; row++) {
            for (let column = 0; column < this.size; column++) {
                if (!this.snake.some(segment => segment.column === column && segment.row === row)) empty.push({ column, row });
            }
        }
        this.food = Phaser.Utils.Array.GetRandom(empty);
    }

    draw() {
        const graphic = this.board;
        graphic.clear();
        for (let row = 0; row < this.size; row++) {
            for (let column = 0; column < this.size; column++) {
                graphic.fillStyle((row + column) % 2 ? 0xe6f0e3 : 0xf0f6e9);
                graphic.fillRect(column * this.cell, row * this.cell, this.cell, this.cell);
            }
        }
        if (this.food) {
            graphic.fillStyle(0xd88b13);
            graphic.fillCircle(this.food.column * this.cell + 12, this.food.row * this.cell + 13, 8);
            graphic.fillStyle(0x18794e);
            graphic.fillEllipse(this.food.column * this.cell + 15, this.food.row * this.cell + 4, 8, 4);
        }
        this.snake.forEach((segment, index) => {
            graphic.fillStyle(index === 0 ? 0x125b3c : 0x319563);
            graphic.fillRoundedRect(segment.column * this.cell + 1, segment.row * this.cell + 1, 22, 22, 5);
        });
        const head = this.snake[0];
        const facing = snakeDirections[this.direction];
        graphic.fillStyle(0xffffff);
        for (const side of [-1, 1]) {
            graphic.fillCircle(head.column * this.cell + 12 + facing.column * 5 + facing.row * side * 5,
                head.row * this.cell + 12 + facing.row * 5 + facing.column * side * 5, 2.5);
        }
        document.getElementById('snake-board').setAttribute('aria-label', `Snake game board, score ${this.score}, length ${this.snake.length}`);
    }
}

const snakeGame = new Phaser.Game({
    type: Phaser.CANVAS,
    width: 480,
    height: 480,
    parent: 'snake-board',
    backgroundColor: '#e6f0e3',
    render: { antialias: false },
    audio: { noAudio: true },
    scene: SnakeScene
});