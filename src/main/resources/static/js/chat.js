'use strict';

var stompClient = null;
var username = null;
var roomId = null;
var typingTimer = null;
var isTyping = false;
var TYPING_TIMEOUT = 500; // debounce 500ms

function connect() {
    username = document.querySelector('#username').value;
    roomId = document.querySelector('#roomId').value;

    if (username && roomId) {
        var socket = new SockJS('/ws');
        stompClient = Stomp.over(socket);

        // tat log debug cua stomp
        stompClient.debug = null;

        stompClient.connect({}, onConnected, onError);
    }
}

function onConnected() {
    // subscribe vao room
    stompClient.subscribe('/topic/room.' + roomId, onMessageReceived);

    // subscribe vao typing indicator
    stompClient.subscribe('/topic/room.' + roomId + '.typing', onTypingReceived);

    // subscribe vao danh sach user online
    stompClient.subscribe('/topic/room.' + roomId + '.users', onUsersUpdate);

    // gui thong bao user join
    stompClient.send('/app/chat.addUser/' + roomId, {},
        JSON.stringify({
            sender: username,
            type: 'JOIN'
        })
    );

    // bat su kien typing tren input
    var messageInput = document.querySelector('#message');
    messageInput.addEventListener('input', handleTypingInput);

    console.log('Da ket noi thanh cong!');
}

function onError(error) {
    console.error('Khong the ket noi WebSocket: ', error);
    var messageArea = document.querySelector('#messageArea');
    var errorElement = document.createElement('div');
    errorElement.className = 'message event';
    errorElement.textContent = 'Khong the ket noi den server. Vui long thu lai!';
    messageArea.appendChild(errorElement);
}

function handleTypingInput() {
    if (!stompClient || !stompClient.connected) return;

    // gui typing = true neu chua gui
    if (!isTyping) {
        isTyping = true;
        sendTypingStatus(true);
    }

    // reset timer moi lan go phim
    clearTimeout(typingTimer);

    // sau 500ms khong go -> gui typing = false
    typingTimer = setTimeout(function() {
        isTyping = false;
        sendTypingStatus(false);
    }, TYPING_TIMEOUT);
}

function sendTypingStatus(typing) {
    stompClient.send('/app/chat.typing/' + roomId, {},
        JSON.stringify({
            username: username,
            roomId: roomId,
            isTyping: typing
        })
    );
}

function onTypingReceived(payload) {
    var typingEvent = JSON.parse(payload.body);
    var typingIndicator = document.querySelector('#typingIndicator');

    // khong hien thi typing cua chinh minh
    if (typingEvent.username === username) return;

    if (typingEvent.typing || typingEvent.isTyping) {
        typingIndicator.textContent = typingEvent.username + ' dang go...';
        typingIndicator.style.display = 'block';
    } else {
        typingIndicator.textContent = '';
        typingIndicator.style.display = 'none';
    }
}

function onUsersUpdate(payload) {
    var users = JSON.parse(payload.body);
    console.log('Online users: ', users);
}

function sendMessage(event) {
    event.preventDefault();
    var messageInput = document.querySelector('#message');
    var messageContent = messageInput.value.trim();

    if (messageContent && stompClient) {
        var chatMessage = {
            sender: username,
            content: messageContent,
            type: 'CHAT',
            roomId: roomId
        };

        stompClient.send('/app/chat.sendMessage/' + roomId, {},
            JSON.stringify(chatMessage));
        messageInput.value = '';

        // tat typing indicator khi gui tin nhan
        if (isTyping) {
            isTyping = false;
            clearTimeout(typingTimer);
            sendTypingStatus(false);
        }
    }
}

function onMessageReceived(payload) {
    var message = JSON.parse(payload.body);
    var messageArea = document.querySelector('#messageArea');
    var messageElement = document.createElement('div');

    if (message.type === 'JOIN') {
        messageElement.className = 'message event';
        messageElement.textContent = message.sender + ' da tham gia phong chat!';
    } else if (message.type === 'LEAVE') {
        messageElement.className = 'message event';
        messageElement.textContent = message.sender + ' da roi phong chat.';
    } else {
        // tin nhan binh thuong
        var isSent = message.sender === username;
        messageElement.className = 'message ' + (isSent ? 'sent' : 'received');

        if (!isSent) {
            var senderName = document.createElement('div');
            senderName.className = 'sender-name';
            senderName.textContent = message.sender;
            messageElement.appendChild(senderName);
        }

        var textElement = document.createElement('div');
        textElement.className = 'message-text';
        textElement.textContent = message.content;
        messageElement.appendChild(textElement);

        var timeElement = document.createElement('div');
        timeElement.className = 'message-time';
        var now = new Date();
        timeElement.textContent = now.getHours().toString().padStart(2, '0') + ':' +
                                  now.getMinutes().toString().padStart(2, '0');
        messageElement.appendChild(timeElement);
    }

    messageArea.appendChild(messageElement);

    // tu dong scroll xuong tin nhan moi nhat
    messageArea.scrollTop = messageArea.scrollHeight;
}

// khoi tao khi trang load xong
document.addEventListener('DOMContentLoaded', function() {
    var messageForm = document.querySelector('#messageForm');
    messageForm.addEventListener('submit', sendMessage, true);

    // tao typing indicator element
    var chatInput = document.querySelector('.chat-input');
    var typingDiv = document.createElement('div');
    typingDiv.id = 'typingIndicator';
    typingDiv.className = 'typing-indicator';
    typingDiv.style.display = 'none';
    chatInput.parentNode.insertBefore(typingDiv, chatInput);

    connect();
});
