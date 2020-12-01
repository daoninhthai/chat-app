'use strict';

// danh sach emoji pho bien
var EMOJI_DATA = [
    '😀', '😃', '😄', '😁', '😂', '🤣', '😊', '😇',
    '🙂', '😉', '😍', '🥰', '😘', '😗', '😋', '😛',
    '😜', '🤪', '😝', '🤗', '🤔', '🤭', '😐', '😑',
    '😶', '😏', '😒', '🙄', '😬', '😌', '😔', '😪',
    '😢', '😭', '😤', '😠', '😡', '🤬', '😱', '😨',
    '😰', '😥', '😓', '🤩', '😎', '🤠', '🥳', '😺',
    '❤️', '🧡', '💛', '💚', '💙', '💜', '🖤', '💕',
    '💗', '💓', '💔', '❣️', '💯', '💢', '💥', '🔥',
    '👍', '👎', '👊', '✊', '🤞', '✌️', '🤟', '👌',
    '👏', '🙌', '🤝', '🙏', '💪', '🎉', '🎊', '🎈',
    '⭐', '🌟', '✨', '🎯', '🏆', '🥇', '🎮', '🎵'
];

/**
 * Tao emoji picker popup
 * @param {HTMLElement} targetInput - input element de chen emoji vao
 * @returns {HTMLElement} emoji picker element
 */
function createEmojiPicker(targetInput) {
    var picker = document.createElement('div');
    picker.className = 'emoji-picker';
    picker.id = 'emojiPicker';

    var header = document.createElement('div');
    header.className = 'emoji-picker-header';
    header.textContent = 'Chon emoji';
    picker.appendChild(header);

    var grid = document.createElement('div');
    grid.className = 'emoji-grid';

    EMOJI_DATA.forEach(function(emoji) {
        var btn = document.createElement('button');
        btn.type = 'button';
        btn.className = 'emoji-btn';
        btn.textContent = emoji;
        btn.title = emoji;
        btn.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();
            insertEmoji(targetInput, emoji);
        });
        grid.appendChild(btn);
    });

    picker.appendChild(grid);

    return picker;
}

/**
 * Chen emoji vao vi tri con tro cua input
 * @param {HTMLInputElement} input - input element
 * @param {string} emoji - emoji can chen
 */
function insertEmoji(input, emoji) {
    var start = input.selectionStart;
    var end = input.selectionEnd;
    var text = input.value;

    // chen emoji vao vi tri con tro
    input.value = text.substring(0, start) + emoji + text.substring(end);

    // dat lai vi tri con tro sau emoji
    var newPos = start + emoji.length;
    input.setSelectionRange(newPos, newPos);
    input.focus();

    // trigger input event de typing indicator hoat dong
    var event = new Event('input', { bubbles: true });
    input.dispatchEvent(event);
}

/**
 * Toggle hien thi / an emoji picker
 */
function toggleEmojiPicker() {
    var picker = document.querySelector('#emojiPicker');
    if (picker) {
        if (picker.style.display === 'none' || picker.style.display === '') {
            picker.style.display = 'block';
        } else {
            picker.style.display = 'none';
        }
    }
}

// dong emoji picker khi click ra ngoai
document.addEventListener('click', function(e) {
    var picker = document.querySelector('#emojiPicker');
    var emojiToggle = document.querySelector('#emojiToggle');
    if (picker && emojiToggle && !picker.contains(e.target) && !emojiToggle.contains(e.target)) {
        picker.style.display = 'none';
    }
});
