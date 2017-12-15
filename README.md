# <h1>BPM Counter</h1>

<p>This is not a fancy app, but a well working algorithm,  based on autocorellation.</p>

Works as follows:
<ul>
  <li>Downsample the signal if necessary</li>  
  <li>Take samples absolult value</li> 
  <li>Run a lowpass filter to extract the envelope</li> 
  <li>Run autocorellation</li> 
  <li>Find autocorellation max value</li>
  <li>Calculate BPM from max value position</li> 
</ul>
