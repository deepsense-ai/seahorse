# Loading spinner

deepsense.ai loading spinner component

### Dependencies

 * angular
 * bootstrap
 * font-awesome

### API

COMPONENT MODULE `deepsense.spinner`

COMPONENT DIRECTIVES  
  `<deepsense-loading-spinner-sm>`  
  `<deepsense-loading-spinner-lg>`  
  `<deepsense-loading-spinner-processing>`
  
EXAMPLE OF USAGE

    <deepsense-loading-spinner-lg></deepsense-loading-spinner-lg>
    <deepsense-loading-spinner-processing
      bg='true'>Wait</deepsense-loading-spinner-processing>

ARGUMENTS

`-sm` and `-lg`  
  NO ARGUMENTS

`-processing`

  - `transclude = true` | {HTMLString} | Means that you can write any text to show
  - `bg` | {String:'true'|'false'} | Show or not background

### VERSION

1.0.2
