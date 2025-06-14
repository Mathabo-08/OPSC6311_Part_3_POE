package com.example.zenith

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * Adapter for the RecyclerView in MonthlyBudget screen.
 * It takes a list of BudgetItem objects and displays them using budget_item.xml layout.
 *
 * @param budgetList A mutable list of BudgetItem objects that this adapter will display.
 */
class BudgetAdapter(private val budgetList: MutableList<BudgetItem>) :
    RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder>() {

    // Optional listener to handle clicks on individual budget items in the RecyclerView.
    // This can be used to open an edit form when a budget card is tapped.
    private var onItemClickListener: ((BudgetItem) -> Unit)? = null

    /**
     * Sets a click listener for items in the RecyclerView.
     * @param listener A lambda function that will be invoked when an item is clicked,
     * passing the clicked BudgetItem as an argument.
     */
    fun setOnItemClickListener(listener: (BudgetItem) -> Unit) {
        onItemClickListener = listener
    }

    /**
     * ViewHolder class to hold references to the UI elements of a single budget_item.xml layout.
     * This improves performance by avoiding repeated findViewById calls.
     * @param itemView The root View of a single budget item layout (budget_item.xml).
     */
    class BudgetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryTitle: TextView = itemView.findViewById(R.id.categoryTitle)
        val allocatedAmount: TextView = itemView.findViewById(R.id.allocatedAmount)
        val spentAmount: TextView = itemView.findViewById(R.id.spentAmount)
        val overspentWarning: TextView = itemView.findViewById(R.id.overspentWarning)
    }

    /**
     * Called when RecyclerView needs a new [BudgetViewHolder] of the given type to represent
     * an item. This new ViewHolder should be constructed with a new View that can represent
     * the items of the given type.
     * @param parent The ViewGroup into which the new View will be added after it is bound to
     * an adapter position.
     * @param viewType The view type of the new View.
     * @return A new BudgetViewHolder that holds a View of the given view type.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetViewHolder {
        // Inflate the budget_item.xml layout for each item
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.budget_item, // The layout for an individual item
            parent,
            false // Do not attach to root immediately, RecyclerView will do it
        )
        return BudgetViewHolder(itemView)
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     * This method updates the contents of the [itemView] to reflect the item at the given position.
     * @param holder The ViewHolder which should be updated to represent the contents of the
     * item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    override fun onBindViewHolder(holder: BudgetViewHolder, position: Int) {
        val currentItem = budgetList[position]

        // Populate TextViews with data from the current BudgetItem
        holder.categoryTitle.text = currentItem.category
        holder.allocatedAmount.text = "Allocated : R${String.format("%.2f", currentItem.allocatedAmount)}"
        holder.spentAmount.text = "Spent : R${String.format("%.2f", currentItem.spentAmount)}"

        // Logic for showing/hiding the overspent warning message
        if (currentItem.spentAmount > currentItem.allocatedAmount) {
            val overspentBy = currentItem.spentAmount - currentItem.allocatedAmount
            holder.overspentWarning.text = "${currentItem.category.uppercase()}: Overspent by R${String.format("%.2f", overspentBy)}."
            holder.overspentWarning.visibility = View.VISIBLE // Make warning visible
        } else {
            holder.overspentWarning.visibility = View.GONE // Hide warning if not overspent
        }

        // Set click listener for the entire item view
        holder.itemView.setOnClickListener {
            onItemClickListener?.invoke(currentItem) // Invoke the lambda if a listener is set
        }
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     * @return The total number of items in this adapter.
     */
    override fun getItemCount() = budgetList.size

    /**
     * Utility function to update the adapter's data set and notify the RecyclerView to refresh.
     * This is typically called when new data is fetched from Firestore.
     * @param newList The new list of BudgetItem objects to display.
     */
    fun updateList(newList: List<BudgetItem>) {
        budgetList.clear() // Clear existing data
        budgetList.addAll(newList) // Add new data
        notifyDataSetChanged() // Notify RecyclerView that the data set has changed, triggering a redraw
    }
}
