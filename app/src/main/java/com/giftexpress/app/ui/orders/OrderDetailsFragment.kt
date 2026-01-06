package com.giftexpress.app.ui.orders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.giftexpress.app.databinding.FragmentOrderDetailsBinding
import com.giftexpress.app.data.model.Order
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OrderDetailsFragment : Fragment() {

    private var _binding: FragmentOrderDetailsBinding? = null
    private val binding get() = _binding!!
    
    // In a real app, we would use SafeArgs to pass the order ID
    // private val args: OrderDetailsFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Retrieve order ID from arguments (assuming we pass it)
        val orderId = arguments?.getString("orderId")
        
        setupListeners()
        loadOrderDetails(orderId)
    }

    private fun setupListeners() {
        binding.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }
        
        binding.btnCancel.setOnClickListener {
            // Handle cancel order
        }
        
        binding.tvChangeAddress.setOnClickListener {
            // Handle change address
        }
    }

    private fun loadOrderDetails(orderId: String?) {
        // In a real app, fetch from ViewModel/Repository using orderId
        // For now, we'll just display mock data or find the order from a shared source if possible
        // Since we don't have a shared repository with data yet, I'll just hardcode one example 
        // or try to find it from the list if I can access it (which I can't easily here without a shared ViewModel)
        
        // Mock data for display
        binding.tvOrderId.text = "Order #264654563444HIURM"
        binding.tvProductName.text = "Tease Candy Noir by Victoria's Secret..."
        binding.tvSize.text = "Size: 3.4 oz (10 ML)"
        binding.tvQuantity.text = "Qty: 1"
        
        binding.tvStatusTitle.text = "Order Placed"
        binding.tvStatusDate.text = "Delivery by Web, 07 Jan"
        
        binding.tvAddressName.text = "Ashwani Sharma"
        binding.tvAddressDetails.text = "H-64, Business Park, 1st Floor, Office, 8, Sector 63 Rd, Sector 63, Noida, Uttar Pradesh 201309\n9858945487"
        
        binding.tvListingPrice.text = "$120.00"
        binding.tvSpecialPrice.text = "$35.00"
        binding.tvDiscount.text = "-$85"
        binding.tvTotalAmount.text = "$35.00"
        
        binding.tvPaymentMethod.text = "Credit Card"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
