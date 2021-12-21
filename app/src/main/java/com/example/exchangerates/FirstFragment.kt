package com.example.exchangerates

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.navigation.fragment.findNavController
import com.example.exchangerates.databinding.FragmentFirstBinding

import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.NodeList
import org.xml.sax.InputSource
import java.lang.Exception
import java.net.URL
import java.net.URI
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    var countries = mutableMapOf<String, Rate>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)

        Thread {
            try {

                val url = URL("http://www.cbr.ru/scripts/XML_daily.asp")
                val dbf: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
                val db: DocumentBuilder = dbf.newDocumentBuilder()
                val doc: Document = db.parse(InputSource(url.openStream()))
                doc.documentElement.normalize()

                val nodeList: NodeList = doc.getElementsByTagName("Valute")

                for (i in 0 until nodeList.length) {
                    var node = nodeList.item(i) as Element
                    var childs = node.childNodes

                    var currentRate = Rate()

                    for(j in 0 until childs.length) {
                        var child = childs.item(j)

                        var nodeName = child.nodeName
                        var nodeContent = child.textContent
                        nodeContent = nodeContent.replace(',', '.')

                        when (nodeName) {
                            "NumCode" -> currentRate.NumCode = nodeContent.toInt()
                            "CharCode" -> currentRate.CharCode = nodeContent
                            "Nominal" -> currentRate.Nominal = nodeContent.toDouble()
                            "Name" -> currentRate.Name = nodeContent
                            "Value" -> currentRate.Cost = nodeContent.toDouble()
                        }
                    }
                    countries[childs.item(3).textContent] = currentRate
                }

            } catch (err: Throwable) {
                Log.d("aaa xxx", err.toString())
            }

            updateSpinner()
        }.start()


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonFirst.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }


        var names = arrayOf("Бразилия", "Аргентина", "Колумбия", "Чили", "Уругвай");
        val selection: TextView = requireView().findViewById(R.id.sumInOtherCurrency)

        var spinner = requireView().findViewById(R.id.spinner) as Spinner;
        // Создаем адаптер ArrayAdapter с помощью массива строк и стандартной разметки элемета spinner
        var adapter = ArrayAdapter(requireView().context, android.R.layout.simple_spinner_item, names);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Применяем адаптер к элементу spinner
        spinner.setAdapter(adapter);

        var button = requireView().findViewById(R.id.updateSum) as Button
        binding.updateSum.setOnClickListener {
            val price: TextView = requireView().findViewById(R.id.sumInRub)
            var userPrice : Int
            try {
                userPrice = price.text.toString().toInt()

                Log.d("user", userPrice.toString())
            } catch (err: Throwable) {
                userPrice = 0
            }

            var selectedItem = spinner.selectedItem.toString()

            updateValue(userPrice, selectedItem)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun updateValue(userPrice : Int, selectedItem : String) {
        var resultSumTV = requireView().findViewById(R.id.sumInOtherCurrency) as TextView
        resultSumTV.text = (userPrice / countries[selectedItem]!!.Cost * countries[selectedItem]!!.Nominal).toString()

    }

    fun updateSpinner() {
        val names: Array<String> = countries.keys.toTypedArray();
        val selection: TextView = requireView().findViewById(R.id.sumInOtherCurrency)

        var spinner = requireView().findViewById(R.id.spinner) as Spinner;
        // Создаем адаптер ArrayAdapter с помощью массива строк и стандартной разметки элемета spinner
        var adapter = ArrayAdapter(requireView().context, android.R.layout.simple_spinner_item, names);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Применяем адаптер к элементу spinner
        spinner.setAdapter(adapter);
    }

}

class Rate {
    public var Name: String = "";
    public var Cost: Double = 0.0;
    public var Nominal: Double = 0.0;
    public var CharCode: String = "";
    public var NumCode: Int = 0;
}