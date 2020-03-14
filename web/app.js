/*global $, document, window, hello*/
var theHBPToken = null;
function getHBPToken() { return theHBPToken; }

var theHBPContext = null;
function getHBPContext() { return theHBPContext; }

var theHBPResponse = null;
function getHBPResponse() { return theHBPResponse; }

function getHBPRequest(sourceURL) {
    var xhr = new XMLHttpRequest();
    xhr.open('GET', sourceURL, true);
    xhr.setRequestHeader("Authorization", "Bearer " + getHBPToken());
    xhr.onload = function () {
        console.log("onload");
        console.log(this.responseText);
        console.log(this);
    };
    console.log(sourceURL);
    xhr.send(null);
}

function postHBPRequest(targetURL, jsonPayload) {
   // console.log('targetURL', targetURL);
   // console.log('jsonPayload', jsonPayload);
   var xhr = new XMLHttpRequest();
   xhr.open('POST', targetURL, true);
   xhr.setRequestHeader("Authorization", "Bearer " + getHBPToken());
   xhr.setRequestHeader("Content-Type", "application/json; charset=UTF-8");
   // xhr.setRequestHeader("Content-Type", "application/json");
   xhr.responseType = 'text';
   theHBPResponse = null;
   xhr.onload = function () {
      if (this.status === 201) {
         // theHBPResponse = this.getAllResponseHeaders();
         theHBPResponse = this.getResponseHeader("Location");
         // Location header is present in response, but cannot be accessed from JavaScript
         // due to broken CORS OPTIONS response on the HBP server side
         // https://stackoverflow.com/questions/8945587/xmlhttprequest-getallresponseheaders-not-returning-all-the-headers
         if (!theHBPResponse)
            theHBPResponse = this.responseText;
         if (!theHBPResponse)
            theHBPResponse = "Status " + this.status;
      }
      else if (xhr.status === 200)
         theHBPResponse = this.responseText;
      else
         theHBPResponse = "Status " + this.status;
      console.log('response', theHBPResponse);
   };

   xhr.send(jsonPayload);
}

(function() {
  'use strict';

  var init = function() {
    // Setup OpenID connect authentication using the clientId provided
    // in the HBP OIDC client page.
    // https://collab.humanbrainproject.eu/#/collab/54/nav/1051
    hello.init({
      hbp: '0f4d498f-c043-42ea-b39e-d129c442af38'
    });
    // If the user is not authenticated, it will redirect to the HBP auth server
    // and use an OpenID connect implicit flow to retrieve an user access token.
    hello.login('hbp', {display: 'page', force: false});

    $(document).ready(function() {
      var auth = hello.getAuthResponse('hbp');
      if (auth && auth.access_token) {
         theHBPToken = auth.access_token;
         // console.log("theHBPToken", theHBPToken);
      }
      theHBPContext = window.location.search.substr(
         window.location.search.indexOf('ctx=') + 4,
         36 // UUID is 36 chars long.
      );
      // console.log("theHBPContext", theHBPContext);
    });
  }

  // Extract the context UUID from the querystring.
  var extractCtx = function() {
    return window.location.search.substr(
      window.location.search.indexOf('ctx=') + 4,
      36 // UUID is 36 chars long.
    );
  };

  init();
}());
