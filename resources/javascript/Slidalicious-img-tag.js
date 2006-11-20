// Slidalicious - Slideshow, v. 0.5.0
// Copyright (c) 2006 Flurin Egger, DigitPaint B.V. (http://www.digitpaint.nl)
// Improved by eiki@idega.is, Idega Software 2006
// ======================================= //

var Slidalicious = Class.create();
var swapSlide;

Slidalicious.prototype = {
      initialize: function(){
            if(arguments.length > 0) this.element = arguments[0];
            if(arguments.length > 1) { this.setOptions(arguments[1]); }
            else { this.setOptions({}); }
            
            this.display_time = this.options.display_time;
            this.transition_time = this.options.transition_time;
            this.transition_effect = this.options.transition_effect;
            this.images = this.options.images;
            this.links = this.options.links;
            this.width = this.options.width;
            this.height = this.options.height;
			this.slides = new Array();
      },
  setOptions: function(options) {
    this.options = Object.extend({
      transition_effect: Effect.Appear, // transition effect
      transition_time: 1.0,           // seconds
      display_time: 5.0                                // seconds
    }, options || {});
  },  
      play: function(){
            if(this.images.length < 1) return this.error("Please specify at least one image.")
            if(!this.initialized) this.initializeSlideshow(); 
            if(!this.initialized) return false;
            this.position = 1;
            
			//no longer needed since we load all slides in the beginning
            //this.setSlide(this.current_slide,0);
            this.setupSlideshowSize();
            
            if(this.images.length > 1){
				  //no longer needed since we load all slides in the beginning
                  //this.setSlide(this.next_slide,this.position);
                  this.timer = 0;
                  if(!this.interval) this.interval = setInterval(this.loop.bind(this),1000);
            } 
      },
      stop: function(){
      	if(this.interval){
      		clearInterval(this.interval);
      		this.interval = null;
             this.clearChildren(this.element);
             this.initialized = false;
      	}
      },
      loop: function(){
            this.timer += 1;
            if(this.timer >= this.display_time){
                  this.transition();
                  this.timer = 0;
            }
      },
      transition: function(){
            this.transition_effect(this.next_slide,{
                        duration:this.transition_time,
                        afterFinish: this.prepareNext.bind(this)});
      },
      prepareNext: function(){
            this.position += 1;
		
		//TODO FIX FOR SPECIAL CASE WHEN THERE ARE ONLY 2 IMAGES
            if(this.position >= this.images.length) this.position = 0;
        
	        if(this.swapSlide){
	        	this.element.removeChild(this.swapSlide);
	        }
			
			this.swapSlide = this.current_slide;
            this.current_slide = this.next_slide;
			this.next_slide = this.slides[this.position];
		    
			this.setupTopSlide(this.next_slide);
			this.element.appendChild(this.next_slide);
			
			this.setupBottomSlide(this.current_slide);
			this.element.appendChild(this.current_slide);
			
			
			//hide it will be removed come next prepare...
			if(this.swapSLide){
				this.setupTopSlide(this.swapSlide);
			}
			
			
			//not needed any more since we preload all slides, see initializeSlides()
            //this.setSlide(this.next_slide,this.position);
      },
     /*setSlide: function(slide,position){
            slide.image.src = this.images[position];
            if(this.links.length > position && this.links[position]){
                  slide.href = this.links[position];
            } else {
                  slide.href = "#";
            }
      },*/
	  initializeSlides: function(){
	  	//This method construct all the slides at once
		//each slide object is a link with an embedded image
		//todo set size now, or maybe we have to wait until displayed?
			for(i=0; i<this.images.length; i++){
				aSlide = document.createElement("A");
	           	aSlidesImage = document.createElement("IMG");
				
				aSlidesImage.src = this.images[i];
	            if(this.links.length > i && this.links[i]){
	                  aSlide.href = this.links[i];
	            } else {
	                  aSlide.href = "#";
	            }
			
				aSlide.image = aSlidesImage;
	            aSlide.appendChild(aSlidesImage);
	           
				this.slides.push(aSlide);
				
				//aSlide.id = "slide_"+this.slides.length;
				
			}
			
			
      },
      initializeSlideshow: function(){
	  	//todo preload the images or at least 2 ahead in time.	
            this.element = $(this.element);
            if(!this.element) return this.error("Could not find element '"+this.element+"' ");
            this.element.style.position = "relative";
			
            this.initializeSlides();
			
            this.next_slide = this.slides[1];
            this.current_slide = this.slides[0];      
           
		   
            this.setupTopSlide(this.next_slide);
            this.setupBottomSlide(this.current_slide);
            
                                    
            this.element.appendChild(this.current_slide);
            this.element.appendChild(this.next_slide);   
			
			                     
            this.initialized = true;
      },
	  setupSlideshowSize: function(){
            var dims = Element.getDimensions(this.current_slide.image);
            var d = {};
            if(!this.width) this.width = dims.width;
            if(!this.height) this.height = dims.height;
            d.width = this.width + "px";
            d.height = this.height + "px";
            if(dims.width > 0 && dims.height > 0) Element.setStyle(this.element,d);
      },
      setupTopSlide: function(element){
            this.setupSlide(element);
            element.style.zIndex = 1;
            element.style.visibility = "hidden";
      },
      setupBottomSlide: function(element){
            this.setupSlide(element);
            element.style.zIndex = 0;
            element.style.visibility = "visible";                     
      },
      setupSlide: function(element){
            element.style.border = 0;
            if(element.image) element.image.style.border = 0;
            element.style.position = "absolute";
            element.style.top = 0;
            element.style.left = 0;
      },
      error: function(msg){
            alert("Slidalicious Error: " + msg);
            return false;
      }
      ,
      clearChildren: function(node){
      	// This is one way to remove all children from a node
		// box is an object refrence to an element with children
		while (node.firstChild){
		    //The list is LIVE so it will re-index each call
		    node.removeChild(node.firstChild);
		 };
      }
}