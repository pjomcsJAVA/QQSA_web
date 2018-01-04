window.seconds = 10;
var x = setInterval(function() {
  window.seconds = window.seconds-1;
  // Display the result in the element with id="demo"
  document.getElementById("timeLeft").innerHTML =  window.seconds + "s ";
  // If the count down is finished, write some text 
  if ( window.seconds < 0) {
    clearInterval(x);
    document.getElementById("timeLeft").innerHTML = "&#8987; EXPIRED";
    $(".correct-answer").show();
    $(".number").hide();
    $(".disable-next-button").removeClass("disable-next-button");
    $(".timer-animation").hide();
    $(".explanation").show();
  }
}, 1000);  