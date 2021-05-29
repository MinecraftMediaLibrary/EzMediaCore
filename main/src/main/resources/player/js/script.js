var music = document.getElementById("music");
var playButton = document.getElementById("play");
var pauseButton = document.getElementById("pause");
var playhead = document.getElementById("elapsed");
var timeline = document.getElementById("slider");
var timer = document.getElementById("timer");
var duration;
pauseButton.style.visibility = "hidden";

var timelineWidth = timeline.offsetWidth - playhead.offsetWidth;
music.addEventListener("timeupdate", timeUpdate, false);

function timeUpdate() {
    var playPercent = timelineWidth * (music.currentTime / duration);
    playhead.style.width = playPercent + "px";

    var secondsIn = Math.floor(((music.currentTime / duration) / 3.5) * 100);
    if (secondsIn <= 9) {
        timer.innerHTML = "0:0" + secondsIn;
    } else {
        timer.innerHTML = "0:" + secondsIn;
    }
}

function playSong() {
  music.play();
  playButton.style.visibility = "hidden";
  pause.style.visibility = "visible";
}

function pauseSong() {
  music.pause();
  playButton.style.visibility = "visible";
  pause.style.visibility = "hidden";
}

playButton.onclick = function() {
  playSong()
}

pauseButton.onclick = function() {
  pauseSong()
}

music.addEventListener("canplaythrough", function() {
    duration = music.duration;
}, false);