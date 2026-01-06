package com.giftexpress.app.ui.orders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.giftexpress.app.R
import com.giftexpress.app.data.model.Order
import com.giftexpress.app.databinding.FragmentOrdersBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OrdersFragment : Fragment() {

    private var _binding: FragmentOrdersBinding? = null
    private val binding get() = _binding!!
    private val ordersAdapter = OrdersAdapter { order ->
        val action = OrdersFragmentDirections.actionOrdersFragmentToOrderDetailsFragment(order.id)
        findNavController().navigate(action)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupListeners()
        loadMockData()
    }

    private fun setupRecyclerView() {
        binding.rvOrders.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = ordersAdapter
        }
    }

    private fun setupListeners() {
        binding.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun loadMockData() {
        val mockOrders = listOf(
            Order(
                id = "1",
                status = "Order Placed",
                statusText = "Order Placed",
                date = "Delivery by Web, 07 Jan",
                dateText = "Delivery by Web, 07 Jan",
                productName = "Versace Eros",
                sizeText = "Size: 3.4 oz (10 ML)",
                quantity = 1,
                statusColor = ContextCompat.getColor(requireContext(), R.color.primary)
            ),
            Order(
                id = "2",
                status = "Delivered",
                statusText = "Delivered",
                date = "Mon, 24 Jan",
                dateText = "Mon, 24 Jan",
                productName = "Versace The Dreamer",
                sizeText = "Size: 3.4 oz (10 ML)",
                quantity = 1,
                statusColor = ContextCompat.getColor(requireContext(), R.color.status_delivered)
            ),
            Order(
                id = "3",
                status = "Order Cancelled",
                statusText = "Order Cancelled",
                date = "As per your request",
                dateText = "As per your request",
                productName = "Violet Eyes",
                sizeText = "Size: 3.4 oz (10 ML)",
                quantity = 1,
                statusColor = ContextCompat.getColor(requireContext(), R.color.discount_red)
            ),
            Order(
                id = "4",
                status = "Order Placed",
                statusText = "Order Placed",
                date = "Delivery by Web, 07 Jan",
                dateText = "Delivery by Web, 07 Jan",
                productName = "Versace Eros",
                sizeText = "Size: 3.4 oz (10 ML)",
                quantity = 1,
                statusColor = ContextCompat.getColor(requireContext(), R.color.primary)
            ),
            Order(
                id = "5",
                status = "Order Placed",
                statusText = "Order Placed",
                date = "Delivery by Web, 07 Jan",
                dateText = "Delivery by Web, 07 Jan",
                productName = "Versace Eros",
                sizeText = "Size: 3.4 oz (10 ML)",
                quantity = 1,
                statusColor = ContextCompat.getColor(requireContext(), R.color.primary)
            )
        )
        ordersAdapter.submitList(mockOrders)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
