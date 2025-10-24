# ViewModel & State Usage in Jetpack Compose

Each screen in this project follows a **ViewModel-driven state pattern** for clear, testable, and reactive UI design.

---

## Structure

ui/<br /> ├── Page/<br /> │ ├── PageScreen.kt<br /> │ ├── PageViewModel.kt<br /> │ └── PgaeState.kt <-- this is usually inside the ViewModel<br />

Each page has:
- **Screen** – Composable function that observes state  
- **ViewModel** – Holds logic and exposes
- **State** – Immutable data representing the current UI  

---

e.g.

```
// ViewModel
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState

    fun logIn() {
        try {
            val response = authRepository.logIn()
            if (response != null) {
                _authState.update { it.copy(isLoggedIn = true) }
            } else {
                _authState.update { it.copy(isLoggedIn = false) }
            }
        } catch (e: Exception) {
            _authState.update { it.copy(error = e.message)}
        }
    }
}

// State
data class AuthState (
    val isLoggedIn: Boolean = false,
    val error: String? = null,
)

// Screen
@Composable
fun LoginScreen (
    authViewModel: AuthViewModel,
) {
    val authState by authViewModel.authState.collectAsState()
    when {
        state.isLoggedIn -> LoginSuccessfullScreen()
        state.error != null -> ErrorView(state.error)
        else -> ItemList(items = state.items)
    }
}
```

# Navigation

Always pass navigation actions as lambda callbacks from ```ui/navigation/MainScreen.tk —
this keeps the screens reusable and free of direct NavController dependencies.

e.g.

```
// LoginScreen.tk
@Composable
fun LoginScreen (
    authViewModel: AuthViewModel,
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val authState by authViewModel.authState.collectAsState()

    when {
        state.isLoggedIn -> onLoginSuccess()
        state.error != null -> ErrorView(state.error)
        else -> ItemList(items = state.items)
    }
}

// MainScreen.tk

      composable("login") {
          LoginScreen(
              authViewModel = authViewModel,
              onLoginSuccess = {
                  navigateWithHistory(navController, navHistory, "weeklyCalendar")
              }
          )
      }
```
