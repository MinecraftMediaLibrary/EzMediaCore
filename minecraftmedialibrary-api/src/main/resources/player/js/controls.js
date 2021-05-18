const wavesurfer = WaveSurfer.create({
    barWidth: 1,
    container: '#wavesurfer',
    cursorWidth: 0,
    dragSelection: true,
    height: 500,
    hideScrollbar: true,
    interact: true,
    normalize: true,
    waveColor: 'rgba(255,255,255,0.05)',
    progressColor: 'rgba(255,255,255,0.15)'
});

$('.player').on('click', '#play', function () {
    if ($(this).hasClass('load')) {
        $(this).removeClass('load');
        wavesurfer.load('https://ia802709.us.archive.org/32/items/GlenMiller-MoonlightSerenade/8a3748ab5a59.mp3');
    } else {
        wavesurfer.pause()
    }
});

let m, s;

function getMinutes(convTime) {
    convTime = Number(convTime);
    m = Math.floor(convTime % 3600 / 60);
    return ((m < 10 ? "0" : "") + m);
}

function getSeconds(convTime) {
    convTime = Number(convTime);
    s = Math.floor(convTime % 3600 % 60);
    return ((s < 10 ? "0" : "") + s);
}

let totalTime, timeJump, currentTime, currentTimeJump;

wavesurfer.on('ready', function () {
    totalTime = wavesurfer.duration();
    timeJump = 300 / totalTime;
    $('.wavesurfer__elem').addClass('show');
    $('.button__loader').fadeOut();
    $('.time__minutes').text(getMinutes(totalTime));
    $('.time__seconds').text(getSeconds(totalTime));
    $('.time, .progress').fadeIn();

    wavesurfer.play();
});

function progressJump() {
    currentTime = wavesurfer.getCurrentTime();
    currentTimeJump = currentTime * timeJump + 10;
    $('.progress__button').css({left: currentTimeJump + 'px'});
    $('.progress__indicator').css({width: currentTimeJump + 'px'});

    $('.time__minutes').text(getMinutes(currentTime));
    $('.time__seconds').text(getSeconds(currentTime));
}

wavesurfer.on('audioprocess', function () {
    progressJump();
});

wavesurfer.on('pause', function () {
    $('.button__play-iconplay').fadeIn();
    $('.button__play-iconpause').fadeOut();
    $('.recordplayer').removeClass('play');
    $('.recordplayer__disc').removeClass('animate');
    $('.artist__image').removeClass('play');
});

wavesurfer.on('play', function () {
    $('.button__play-iconplay').fadeOut();
    $('.button__play-iconpause').fadeIn();
    $('.recordplayer').addClass('play');
    $('.recordplayer__disc').addClass('animate');
    $('.artist__image').addClass('play');
});

wavesurfer.on('loading', function (event) {
    $('.button__loader').css({height: event + 'px'});
});

wavesurfer.on('seek', function () {
    progressJump();
});