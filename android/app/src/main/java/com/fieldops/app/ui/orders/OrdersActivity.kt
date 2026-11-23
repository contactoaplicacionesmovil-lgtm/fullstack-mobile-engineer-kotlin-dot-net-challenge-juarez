package com.fieldops.app.ui.orders

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.core.content.edit
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fieldops.app.LoginActivity
import com.fieldops.app.R
import com.fieldops.app.Session
import com.fieldops.app.ui.theme.FieldOpsTheme

class OrdersActivity : AppCompatActivity() {

    private val viewModel: OrdersViewModel by viewModels { OrdersViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = getString(R.string.orders_title)

        setContent {
            FieldOpsTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                LaunchedEffect(uiState.userMessage) {
                    uiState.userMessage?.let { message ->
                        Toast.makeText(this@OrdersActivity, message, Toast.LENGTH_SHORT).show()
                        viewModel.onUserMessageShown()
                    }
                }

                OrdersScreen(
                    uiState = uiState,
                    fullName = Session.fullName,
                    onFilterSelected = viewModel::onFilterSelected,
                    onOrderStatusChange = viewModel::updateOrderStatus,
                    onRetry = viewModel::loadOrders,
                )
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(0, MENU_LOGOUT, 0, getString(R.string.orders_menu_logout))
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == MENU_LOGOUT) {
            getSharedPreferences("fieldops", MODE_PRIVATE).edit {
                remove("token")
            }
            Session.token = null
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        private const val MENU_LOGOUT = 1
    }
}
